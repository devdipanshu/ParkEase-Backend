package com.parkease.notification;

import com.parkease.notification.dto.NotificationRequest;
import com.parkease.notification.entity.Notification;
import com.parkease.notification.enums.NotificationChannel;
import com.parkease.notification.enums.NotificationType;
import com.parkease.notification.exception.ResourceNotFoundException;
import com.parkease.notification.repository.NotificationRepository;
import com.parkease.notification.service.EmailService;
import com.parkease.notification.service.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .notificationId(1L)
                .recipientId(100L)
                .type(NotificationType.BOOKING_CONFIRMED)
                .channel(NotificationChannel.APP)
                .title("Booking Confirmed")
                .message("Your booking is confirmed")
                .isRead(false)
                .isDelivered(true)
                .build();
    }

    @Test
    void send_appChannel_persistsAndMarksDelivered() {
        NotificationRequest request = new NotificationRequest();
        request.setRecipientId(100L);
        request.setType(NotificationType.BOOKING_CONFIRMED);
        request.setChannel(NotificationChannel.APP);
        request.setTitle("Booking Confirmed");
        request.setMessage("Your booking is confirmed");

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification result = notificationService.send(request);

        assertThat(result.getIsDelivered()).isTrue();
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verifyNoInteractions(emailService);
    }

    @Test
    void send_emailChannel_queuesEmailAsync() {
        NotificationRequest request = new NotificationRequest();
        request.setRecipientId(100L);
        request.setRecipientEmail("user@test.com");
        request.setType(NotificationType.BOOKING_CONFIRMED);
        request.setChannel(NotificationChannel.EMAIL);
        request.setTitle("Booking Confirmed");
        request.setMessage("Your booking is confirmed");

        Notification emailNotification = Notification.builder()
                .notificationId(2L)
                .recipientId(100L)
                .channel(NotificationChannel.EMAIL)
                .isDelivered(false)
                .isRead(false)
                .type(NotificationType.BOOKING_CONFIRMED)
                .title("Booking Confirmed")
                .message("Your booking is confirmed")
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(emailNotification);

        notificationService.send(request);

        verify(emailService).sendAsync(any(Notification.class));
    }

    @Test
    void getById_existingId_returnsNotification() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        Notification result = notificationService.getById(1L);

        assertThat(result.getNotificationId()).isEqualTo(1L);
    }

    @Test
    void getById_unknownId_throwsException() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void markAsRead_unreadNotification_setsIsReadTrue() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification result = notificationService.markAsRead(1L);

        assertThat(result.getIsRead()).isTrue();
        assertThat(result.getReadAt()).isNotNull();
    }

    @Test
    void markAsRead_alreadyRead_returnsWithoutSaving() {
        notification.setIsRead(true);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        Notification result = notificationService.markAsRead(1L);

        assertThat(result.getIsRead()).isTrue();
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAllAsRead_withUnread_marksAllAndSaves() {
        Notification unread1 = Notification.builder().notificationId(1L).isRead(false).build();
        Notification unread2 = Notification.builder().notificationId(2L).isRead(false).build();

        when(notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(100L))
                .thenReturn(List.of(unread1, unread2));

        notificationService.markAllAsRead(100L);

        assertThat(unread1.getIsRead()).isTrue();
        assertThat(unread2.getIsRead()).isTrue();
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    void markAllAsRead_noUnread_doesNothing() {
        when(notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(100L))
                .thenReturn(List.of());

        notificationService.markAllAsRead(100L);

        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    void getUnreadCount_returnsRepositoryCount() {
        when(notificationRepository.countByRecipientIdAndIsReadFalse(100L)).thenReturn(5L);

        Long count = notificationService.getUnreadCount(100L);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    void deleteNotification_existingId_callsDelete() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(1L);

        verify(notificationRepository).deleteById(1L);
    }
}
