package com.parkease.payment.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${notification.service.url:http://localhost:8087}")
public interface NotificationClient {

    @PostMapping("/notifications")
    void send(@RequestBody NotificationRequest request);

    @Data
    class NotificationRequest {
        private Long recipientId;
        private String type;
        private String channel;
        private String title;
        private String message;
    }
}
