package com.parkease.payment.service;

import com.parkease.payment.config.RabbitMQConfig;
import com.parkease.payment.dto.NotificationEvent;
import com.parkease.payment.dto.PaymentRequest;
import com.parkease.payment.dto.RefundRequest;
import com.parkease.payment.entity.Payment;
import com.parkease.payment.enums.PaymentMode;
import com.parkease.payment.enums.PaymentStatus;
import com.parkease.payment.exception.ResourceNotFoundException;
import com.parkease.payment.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReceiptService receiptService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Override
    public Payment processPayment(PaymentRequest request) {
        Payment existing = paymentRepository.findByBookingId(request.getBookingId()).orElse(null);

        if (existing != null && existing.getStatus() == PaymentStatus.PAID) {
            // Already paid — idempotent success
            return existing;
        }

        PaymentMode mode = request.getMode() != null ? request.getMode() : PaymentMode.CASH;

        // Reuse PENDING record if one exists (retry scenario), otherwise create fresh
        Payment payment;
        if (existing != null && existing.getStatus() == PaymentStatus.PENDING) {
            payment = existing;
            payment.setMode(mode);
            payment.setAmount(request.getAmount());
        } else {
            payment = Payment.builder()
                    .bookingId(request.getBookingId())
                    .userId(request.getUserId())
                    .lotId(request.getLotId())
                    .amount(request.getAmount())
                    .mode(mode)
                    .status(PaymentStatus.PENDING)
                    .build();
            payment = paymentRepository.save(payment);
        }

        if (mode == PaymentMode.RAZORPAY) {
            return processRazorpayPayment(payment, request.getTransactionId());
        }
        return processCashPayment(payment);
    }

    private Payment processRazorpayPayment(Payment payment, String razorpayPaymentId) {
        if (razorpayPaymentId == null || razorpayPaymentId.isBlank()) {
            throw new IllegalStateException("Razorpay payment ID is required for RAZORPAY mode.");
        }
        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            com.razorpay.Payment rzpPayment = client.payments.fetch(razorpayPaymentId);
            String status = rzpPayment.get("status");

            if ("captured".equals(status) || "authorized".equals(status)) {
                payment.setTransactionId(razorpayPaymentId);
                payment.setStatus(PaymentStatus.PAID);
                payment.setPaidAt(LocalDateTime.now());
                log.info("[RAZORPAY] Payment verified — id: {}, amount: {}", razorpayPaymentId, payment.getAmount());
                Payment saved = paymentRepository.save(payment);
                sendNotification(saved.getUserId(), "PAYMENT_SUCCESS",
                        "Payment Successful",
                        "Your payment of Rs. " + saved.getAmount() + " for Booking #" + saved.getBookingId() + " was successful. Transaction ID: " + razorpayPaymentId);
                return saved;
            } else {
                log.warn("[RAZORPAY] Payment not captured — id: {}, status: {}", razorpayPaymentId, status);
                throw new IllegalStateException("Razorpay payment not completed (status: " + status + ").");
            }
        } catch (RazorpayException e) {
            log.error("[RAZORPAY] Verification failed — id: {}, error: {}", razorpayPaymentId, e.getMessage());
            throw new IllegalStateException("Razorpay verification failed: " + e.getMessage());
        }
    }

    private Payment processCashPayment(Payment payment) {
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        log.info("[CASH PAYMENT] Recorded — txn: {}, amount: {}", payment.getTransactionId(), payment.getAmount());
        Payment saved = paymentRepository.save(payment);
        sendNotification(saved.getUserId(), "PAYMENT_SUCCESS",
                "Payment Recorded",
                "Your cash payment of Rs. " + saved.getAmount() + " for Booking #" + saved.getBookingId() + " has been recorded.");
        return saved;
    }

    private void sendNotification(Long userId, String type, String title, String message) {
        try {
            NotificationEvent event = NotificationEvent.builder()
                    .recipientId(userId)
                    .type(type)
                    .channel("APP")
                    .title(title)
                    .message(message)
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
            log.info("[NOTIFICATION] Published to RabbitMQ — user: {}, type: {}", userId, type);
        } catch (Exception e) {
            log.warn("[NOTIFICATION] Failed to publish to RabbitMQ for user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        return findOrThrow(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for booking id: " + bookingId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByUser(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    @Override
    public Payment refundPayment(Long paymentId, RefundRequest request) {
        Payment payment = findOrThrow(paymentId);

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException(
                    "Only PAID payments can be refunded (current status: " + payment.getStatus() + ")");
        }
        if (request.getAmount() > payment.getAmount()) {
            throw new IllegalStateException(
                    "Refund amount " + request.getAmount() + " exceeds payment amount " + payment.getAmount());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundAmount(request.getAmount());
        log.info("[MOCK REFUND] Refund processed for txn: {}, amount: {}",
                payment.getTransactionId(), request.getAmount());
        return paymentRepository.save(payment);
    }

    @Override
    public byte[] generateReceipt(Long paymentId) {
        Payment payment = findOrThrow(paymentId);
        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("Receipt can only be generated for PAID payments");
        }
        return receiptService.generate(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getTotalRevenueByLot(Long lotId) {
        return paymentRepository.sumAmountByLotId(lotId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getRevenueBetween(LocalDateTime start, LocalDateTime end) {
        return paymentRepository.sumRevenueBetween(start, end);
    }

    private Payment findOrThrow(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }
}
