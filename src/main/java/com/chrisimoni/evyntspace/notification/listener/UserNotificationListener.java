package com.chrisimoni.evyntspace.notification.listener;

import com.chrisimoni.evyntspace.common.events.LoginCodeNotificationEvent;
import com.chrisimoni.evyntspace.common.events.PasswordResetNotificationEvent;
import com.chrisimoni.evyntspace.common.events.VerificationCodeRequestedEvent;
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

        sendVerificationCodeNotification(
                event.getRecipient(),
                event.getVerificationCode(),
                event.getCodeValidityInMinutes(),
                MessageTemplate.VERIFICATION_NOTIFICATION
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleLoginCodeNotificationEvent(LoginCodeNotificationEvent event) {
        log.info("handleLoginCodeNotificationEvent received for {}.", event.getRecipient());

        sendVerificationCodeNotification(
                event.getRecipient(),
                event.getVerificationCode(),
                event.getCodeValidityInMinutes(),
                MessageTemplate.LOGIN_CODE_NOTIFICATION
        );
    }

    @EventListener
    @Async
    public void handlePasswordResetNotificationEvent(PasswordResetNotificationEvent event) {
        log.info("PasswordResetNotificationEvent received for {}.", event.getRecipient());

        Map<String, Object> templateModel = Map.of(
                RESET_URL_KEY, event.getLink(),
                CODE_VALIDITY_KEY, event.getValidity()
        );

        sendNotification(event.getRecipient(), MessageTemplate.PASSWORD_RESET_NOTIFICATION, templateModel);
    }

    private void sendVerificationCodeNotification(
            String recipient,
            String verificationCode,
            int codeValidityInMinutes,
            MessageTemplate template) {

        Map<String, Object> templateModel = Map.of(
                RECIPIENT, recipient,
                VERIFICATION_CODE_KEY, verificationCode,
                CODE_VALIDITY_KEY, codeValidityInMinutes
        );

        sendNotification(recipient, template, templateModel);
    }

    private void sendNotification(String recipient, MessageTemplate template, Map<String, Object> templateModel) {
        MessageDetails messageDetails = contentBuilder.createMessageDetails(recipient, template, templateModel);
        notificationService.send(messageDetails);
    }
}
