package com.chrisimoni.evyntspace.notification.service.outbox;

import com.chrisimoni.evyntspace.notification.model.MessageDetails;
import com.chrisimoni.evyntspace.notification.model.NotificationOutbox;
import com.chrisimoni.evyntspace.notification.model.NotificationStatus;
import com.chrisimoni.evyntspace.notification.model.NotificationType;
import com.chrisimoni.evyntspace.notification.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationOutboxServiceImpl implements NotificationOutboxService{
    private final NotificationOutboxRepository outboxRepository;

    @Value("${email.outbox.retry-delay-seconds}") // Configurable initial delay for fallback
    private long retryDelaySeconds;

    @Override
    @Transactional
    public void saveToOutbox(MessageDetails messageDetails, NotificationType type, String error) {
        NotificationOutbox notificationOutbox = new NotificationOutbox(messageDetails, type);
        notificationOutbox.setStatus(NotificationStatus.FAILED); // Mark as failed, needs retry
        notificationOutbox.setLastAttemptTime(LocalDateTime.now());
        notificationOutbox.setRetryAttempts(1); // This was the first attempt
        notificationOutbox.setNextAttemptTime(LocalDateTime.now().plusSeconds(retryDelaySeconds));
        String errorMessage = error != null ? error : String.format(
                "Unknown error while sending %s notification",
                type.name().toLowerCase());
        notificationOutbox.setLastError(errorMessage);

        outboxRepository.save(notificationOutbox);
    }
}
