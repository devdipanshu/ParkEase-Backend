package com.parkease.notification.repository;

import com.parkease.notification.entity.Notification;
import com.parkease.notification.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    List<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId);

    Long countByRecipientIdAndIsReadFalse(Long recipientId);

    List<Notification> findByRecipientIdAndType(Long recipientId, NotificationType type);

    List<Notification> findByIsDeliveredFalse();
}
