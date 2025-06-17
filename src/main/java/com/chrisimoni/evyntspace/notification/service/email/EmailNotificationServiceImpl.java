package com.chrisimoni.evyntspace.notification.service.email;

import com.chrisimoni.evyntspace.common.exception.ExternalServiceException;
import com.chrisimoni.evyntspace.notification.model.MessageDetails;
import com.chrisimoni.evyntspace.notification.enums.NotificationType;
import com.chrisimoni.evyntspace.notification.service.NotificationService;
import com.chrisimoni.evyntspace.notification.service.email.gateway.EmailServiceGateway;
import com.chrisimoni.evyntspace.notification.service.outbox.NotificationOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationServiceImpl implements NotificationService {
    private final EmailServiceGateway emailSenderGateway;
    private final NotificationOutboxService outboxService;

    @Override
    @Async
    public void send(MessageDetails messageDetails) {
        try {
            emailSenderGateway.sendEmail(messageDetails);
        }catch (ExternalServiceException e) {
            outboxService.saveToOutbox(messageDetails, NotificationType.EMAIL, e.getMessage());
        }
    }
}
