package com.parkease.notification.service;

import com.parkease.notification.dto.NotificationRequest;
import com.parkease.notification.entity.Notification;
import com.parkease.notification.enums.NotificationChannel;
import com.parkease.notification.exception.ResourceNotFoundException;
import com.parkease.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Override
    public Notification send(NotificationRequest request) {
        Notification notification = Notification.builder()
                .recipientId(request.getRecipientId())
                .recipientEmail(request.getRecipientEmail())
                .type(request.getType())
                .channel(request.getChannel())
                .title(request.getTitle())
                .message(request.getMessage())
                .isRead(false)
                .isDelivered(false)
                .build();

        // Persist first — record exists even if delivery fails
        notification = notificationRepository.save(notification);

        if (request.getChannel() == NotificationChannel.APP) {
            notification.setIsDelivered(true);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            log.info("In-app notification {} created for user {}",
                    notification.getNotificationId(), request.getRecipientId());

        } else if (request.getChannel() == NotificationChannel.EMAIL) {
            emailService.sendAsync(notification);
            log.info("Email notification {} queued for {}",
                    notification.getNotificationId(), request.getRecipientEmail());
        }

        return notification;
    }

    @Override
    public List<Notification> sendBulk(List<NotificationRequest> requests) {
        List<Notification> results = new ArrayList<>();
        for (NotificationRequest req : requests) {
            try {
                results.add(send(req));
            } catch (Exception e) {
                log.error("Failed to queue notification for user {}: {}",
                        req.getRecipientId(), e.getMessage());
            }
        }
        log.info("Bulk send: {}/{} notifications queued", results.size(), requests.size());
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public Notification getById(Long id) {
        return findOrThrow(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getByRecipient(Long recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUnreadByRecipient(Long recipientId) {
        return notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(recipientId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }

    @Override
    public Notification markAsRead(Long notificationId) {
        Notification n = findOrThrow(notificationId);
        if (Boolean.TRUE.equals(n.getIsRead())) {
            return n;
        }
        n.setIsRead(true);
        n.setReadAt(LocalDateTime.now());
        return notificationRepository.save(n);
    }

    @Override
    public void markAllAsRead(Long recipientId) {
        List<Notification> unread = notificationRepository
                .findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(recipientId);
        if (unread.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();
        unread.forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(now);
        });
        notificationRepository.saveAll(unread);
        log.info("Marked {} notifications as read for user {}", unread.size(), recipientId);
    }

    @Override
    public void deleteNotification(Long id) {
        findOrThrow(id);
        notificationRepository.deleteById(id);
    }

    private Notification findOrThrow(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
    }
}
