package com.parkease.notification.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkNotificationRequest {

    @NotEmpty(message = "Notifications list must not be empty")
    @Valid
    private List<NotificationRequest> notifications;
}
