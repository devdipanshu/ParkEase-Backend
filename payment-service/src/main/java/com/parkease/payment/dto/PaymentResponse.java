package com.parkease.payment.dto;

import com.parkease.payment.entity.Payment;
import com.parkease.payment.enums.PaymentMode;
import com.parkease.payment.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {

    private Long paymentId;
    private Long bookingId;
    private Long userId;
    private Long lotId;
    private Double amount;
    private PaymentStatus status;
    private PaymentMode mode;
    private String transactionId;
    private String failureReason;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private Double refundAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PaymentResponse from(Payment p) {
        return PaymentResponse.builder()
                .paymentId(p.getPaymentId())
                .bookingId(p.getBookingId())
                .userId(p.getUserId())
                .lotId(p.getLotId())
                .amount(p.getAmount())
                .status(p.getStatus())
                .mode(p.getMode())
                .transactionId(p.getTransactionId())
                .failureReason(p.getFailureReason())
                .paidAt(p.getPaidAt())
                .refundedAt(p.getRefundedAt())
                .refundAmount(p.getRefundAmount())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
