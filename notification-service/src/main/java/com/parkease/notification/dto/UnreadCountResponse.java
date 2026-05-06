package com.parkease.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UnreadCountResponse {

    private Long recipientId;
    private Long unreadCount;
}
