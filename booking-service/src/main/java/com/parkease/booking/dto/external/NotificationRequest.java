package com.parkease.booking.dto.external;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationRequest {

    private Long recipientId;
    private String recipientEmail;
    private String type;
    private String channel;
    private String title;
    private String message;
}
