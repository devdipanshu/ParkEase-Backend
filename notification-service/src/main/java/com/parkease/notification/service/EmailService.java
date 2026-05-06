package com.parkease.notification.service;

import com.parkease.notification.entity.Notification;
import com.parkease.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;

    @Value("${notification.mock-mode}")
    private boolean mockMode;

    @Value("${notification.email.enabled}")
    private boolean emailEnabled;

    @Value("${notification.email.from-address}")
    private String fromAddress;

    @Value("${notification.email.from-name}")
    private String fromName;

    @Async("notificationExecutor")
    public void sendAsync(Notification notification) {
        try {
            if (mockMode || !emailEnabled) {
                log.info("[MOCK EMAIL] To: {} | Subject: {} | Body: {}",
                        notification.getRecipientEmail(),
                        notification.getTitle(),
                        notification.getMessage());
                markDelivered(notification);
                return;
            }

            if (notification.getRecipientEmail() == null
                    || notification.getRecipientEmail().isBlank()) {
                markFailed(notification, "No email address provided");
                return;
            }

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(notification.getRecipientEmail());
            helper.setSubject(notification.getTitle());
            helper.setText(notification.getTitle() + "\n\n" + notification.getMessage(), false);

            mailSender.send(mimeMessage);
            markDelivered(notification);
            log.info("Email sent to {} — subject: {}",
                    notification.getRecipientEmail(), notification.getTitle());

        } catch (Exception e) {
            log.error("Email send failed for notification {}: {}",
                    notification.getNotificationId(), e.getMessage());
            markFailed(notification, e.getMessage());
        }
    }

    private void markDelivered(Notification n) {
        n.setIsDelivered(true);
        n.setSentAt(LocalDateTime.now());
        notificationRepository.save(n);
    }

    private void markFailed(Notification n, String reason) {
        n.setIsDelivered(false);
        n.setFailureReason(reason);
        notificationRepository.save(n);
    }
}
