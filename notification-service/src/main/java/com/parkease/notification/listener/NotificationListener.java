package com.parkease.notification.listener;

import com.parkease.notification.config.RabbitMQConfig;
import com.parkease.notification.dto.NotificationRequest;
import com.parkease.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void handleNotification(NotificationRequest request) {
        log.info("Received notification event from queue: type={}, recipient={}",
                request.getType(), request.getRecipientId());
        notificationService.send(request);
    }
}
