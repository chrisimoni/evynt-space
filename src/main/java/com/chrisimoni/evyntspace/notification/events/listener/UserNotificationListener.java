package com.chrisimoni.evyntspace.notification.events.listener;

import com.chrisimoni.evyntspace.common.events.PasswordResetNotificationEvent;
import com.chrisimoni.evyntspace.user.events.VerificationCodeRequestedEvent;
import com.chrisimoni.evyntspace.notification.model.MessageDetails;
import com.chrisimoni.evyntspace.notification.enums.MessageTemplate;
import com.chrisimoni.evyntspace.notification.service.NotificationContentBuilder;
import com.chrisimoni.evyntspace.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;

import static com.chrisimoni.evyntspace.notification.constant.NotificationTemplateConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserNotificationListener {
    private final NotificationService notificationService;
    private final NotificationContentBuilder contentBuilder;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleVerificationCodeRequestedEvent(VerificationCodeRequestedEvent event) {
        log.info("VerificationCodeRequestedEvent received for {}.", event.getRecipient());
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put(RECIPIENT, event.getRecipient());
        templateModel.put(VERIFICATION_CODE_KEY, event.getVerificationCode());
        templateModel.put(CODE_VALIDITY_KEY, event.getCodeValidityInMinutes());
        MessageDetails messageDetails = contentBuilder.createMessageDetails(
                event.getRecipient(), MessageTemplate.VERIFICATION_NOTIFICATION, templateModel);

        notificationService.send(messageDetails);
    }

    @EventListener
    @Async
    public void handlePasswordResetNotificationEvent(PasswordResetNotificationEvent event) {
        log.info("PasswordResetNotificationEvent received for {}.", event.getRecipient());
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put(RESET_URL_KEY, event.getLink());
        templateModel.put(RESET_VALIDITY_KEY, event.getValidity());
        MessageDetails messageDetails = contentBuilder.createMessageDetails(
                event.getRecipient(), MessageTemplate.PASSWORD_RESET_NOTIFICATION, templateModel);

        notificationService.send(messageDetails);
    }
}
