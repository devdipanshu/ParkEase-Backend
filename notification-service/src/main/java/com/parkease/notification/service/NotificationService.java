package com.parkease.notification.service;

import com.parkease.notification.dto.NotificationRequest;
import com.parkease.notification.entity.Notification;

import java.util.List;

public interface NotificationService {

    Notification send(NotificationRequest request);

    List<Notification> sendBulk(List<NotificationRequest> requests);

    Notification getById(Long id);

    List<Notification> getByRecipient(Long recipientId);

    List<Notification> getUnreadByRecipient(Long recipientId);

    Long getUnreadCount(Long recipientId);

    Notification markAsRead(Long notificationId);

    void markAllAsRead(Long recipientId);

    void deleteNotification(Long id);
}
