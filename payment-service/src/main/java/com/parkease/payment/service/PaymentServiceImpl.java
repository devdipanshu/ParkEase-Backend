package com.parkease.payment.service;

import com.parkease.payment.dto.PaymentRequest;
import com.parkease.payment.dto.RefundRequest;
import com.parkease.payment.entity.Payment;
import com.parkease.payment.enums.PaymentMode;
import com.parkease.payment.enums.PaymentStatus;
import com.parkease.payment.exception.ResourceNotFoundException;
import com.parkease.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public Payment processPayment(PaymentRequest request) {
        // Idempotency: block double charge for the same booking
        Payment p = paymentRepository.findByBookingId(request.getBookingId()).orElse(null);

        if(p != null && p.getStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException(
                    "Payment already processed for booking: " + request.getBookingId());
        }

        PaymentMode mode = request.getMode() != null ? request.getMode() : PaymentMode.CASH;

        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .userId(request.getUserId())
                .lotId(request.getLotId())
                .amount(request.getAmount())
                .mode(mode)
                .status(PaymentStatus.PENDING)
                .build();
        payment = paymentRepository.save(payment);
        // @PrePersist has auto-generated transactionId at this point

        return processMockPayment(payment);
    }

    private Payment processMockPayment(Payment payment) {
        boolean success = Math.random() > 0.3;
        if (success) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            log.info("[MOCK PAYMENT] SUCCESS — txn: {}, amount: {}",
                    payment.getTransactionId(), payment.getAmount());
            return paymentRepository.save(payment);
        } else {
            log.warn("[MOCK PAYMENT] FAILED — txn: {}", payment.getTransactionId());
            // Throw without saving — transaction rolls back, user can retry cleanly
            throw new IllegalStateException("Payment declined. Please try again with a different method.");
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
