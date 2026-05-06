package com.parkease.notification.dto;

import com.parkease.notification.entity.Notification;
import com.parkease.notification.enums.NotificationChannel;
import com.parkease.notification.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long notificationId;
    private Long recipientId;
    private String recipientEmail;
//    private String recipientPhone;
    private NotificationType type;
    private NotificationChannel channel;
    private String title;
    private String message;
    private Boolean isRead;
    private Boolean isDelivered;
    private String failureReason;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getNotificationId())
                .recipientId(n.getRecipientId())
                .recipientEmail(n.getRecipientEmail())
//                .recipientPhone(n.getRecipientPhone())
                .type(n.getType())
                .channel(n.getChannel())
                .title(n.getTitle())
                .message(n.getMessage())
                .isRead(n.getIsRead())
                .isDelivered(n.getIsDelivered())
                .failureReason(n.getFailureReason())
                .sentAt(n.getSentAt())
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }
}