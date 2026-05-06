package com.parkease.payment.service;

import com.parkease.payment.dto.PaymentRequest;
import com.parkease.payment.dto.RefundRequest;
import com.parkease.payment.entity.Payment;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {

    Payment processPayment(PaymentRequest request);

    Payment getPaymentById(Long id);

    Payment getPaymentByBookingId(Long bookingId);

    List<Payment> getPaymentsByUser(Long userId);

    Payment refundPayment(Long paymentId, RefundRequest request);

    byte[] generateReceipt(Long paymentId);

    Double getTotalRevenueByLot(Long lotId);

    Double getRevenueBetween(LocalDateTime start, LocalDateTime end);
}
