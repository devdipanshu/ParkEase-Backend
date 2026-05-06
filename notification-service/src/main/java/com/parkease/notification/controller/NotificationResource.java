package com.parkease.notification.controller;

import com.parkease.notification.dto.BulkNotificationRequest;
import com.parkease.notification.dto.NotificationRequest;
import com.parkease.notification.dto.NotificationResponse;
import com.parkease.notification.dto.UnreadCountResponse;
import com.parkease.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationResource {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NotificationResponse.from(notificationService.send(request)));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<NotificationResponse>> sendBulk(
            @Valid @RequestBody BulkNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.sendBulk(request.getNotifications()).stream()
                        .map(NotificationResponse::from)
                        .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(NotificationResponse.from(notificationService.getById(id)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getByRecipient(userId).stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList()));
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponse>> getUnread(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadByRecipient(userId).stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList()));
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(
                new UnreadCountResponse(userId, notificationService.getUnreadCount(userId)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(NotificationResponse.from(notificationService.markAsRead(id)));
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
