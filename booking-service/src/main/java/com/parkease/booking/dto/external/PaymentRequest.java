package com.parkease.booking.dto.external;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentRequest {

    private Long userId;
    private Long bookingId;
    private Double amount;
    private String mode;
}
