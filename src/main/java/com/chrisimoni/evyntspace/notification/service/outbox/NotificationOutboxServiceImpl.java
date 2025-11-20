package com.chrisimoni.evyntspace.notification.service.outbox;

import com.chrisimoni.evyntspace.notification.enums.NotificationStatus;
import com.chrisimoni.evyntspace.notification.enums.NotificationType;
import com.chrisimoni.evyntspace.notification.model.MessageDetails;
import com.chrisimoni.evyntspace.notification.model.NotificationOutbox;
import com.chrisimoni.evyntspace.notification.repository.NotificationOutboxRepository;
import com.chrisimoni.evyntspace.notification.service.email.gateway.EmailServiceGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationOutboxServiceImpl implements NotificationOutboxService{
    private final NotificationOutboxRepository outboxRepository;
    private final EmailServiceGateway emailServiceGateway;

    @Value("${notification.outbox.max-retry-attempts}")
    private int maxRetryAttempts;

    @Value("${notification.outbox.initial-retry-interval-minutes}")
    private int initialRetryIntervalMinutes;

    @Value("${notification.outbox.retry-interval-factor}")
    private int retryIntervalFactor;

    @Override
    @Transactional(propagation = Propagation.REQUIRED) // Ensure this save is part of the original transaction
    public void saveFailedMessageToOutbox(MessageDetails messageDetails, NotificationType type, String error) {
        NotificationOutbox notificationOutbox = new NotificationOutbox(messageDetails, type);
        // Calculate next attempt time with exponential backoff (starting from retry attempt 0)
        Instant nextAttemptTime = getNextAttemptTime(0);
        // Mark as FAILED with first retry scheduled
        notificationOutbox.markAsFailed(error, nextAttemptTime);

        outboxRepository.save(notificationOutbox);
        log.info("Saved failed email to outbox. Recipient: {}, Next retry at: {}",
                messageDetails.getRecipient(), nextAttemptTime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationOutbox> findFailedMessagesToProcess(NotificationStatus notificationStatus, Pageable pageable) {
        return outboxRepository.findFailedMessagesToProcess(notificationStatus, pageable);
    }

    /**
     * Processes a single outbox message, attempting to send it and updating its status.
     * This method is intended to be called by the scheduler.
     * It runs in its own transaction to ensure each message's state is updated atomically.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) // Critical: new transaction for each message
    public void processSingleOutboxMessage(NotificationOutbox outboxMessage) {
        log.info("Processing outbox message ID: {} (Type: {}, Recipient: {}, Attempt: {}/{})",
                outboxMessage.getId(),
                outboxMessage.getNotificationType(),
                outboxMessage.getMessageDetails().getRecipient(),
                outboxMessage.getRetryAttempts() + 1,
                maxRetryAttempts);

        MessageDetails details = outboxMessage.getMessageDetails();
        outboxMessage.setLastAttemptTime(Instant.now());
        outboxMessage.setRetryAttempts(outboxMessage.getRetryAttempts() + 1); // Increment attempt count

        try {
            emailServiceGateway.sendEmail(details);
            // --- Mark as SENT on success ---
            outboxMessage.markAsSent();
            log.info("Outbox message ID: {} successfully sent on attempt {}/{}",
                    outboxMessage.getId(), outboxMessage.getRetryAttempts(), maxRetryAttempts);
        } catch (Exception e) {
            // --- Handle failure ---
            log.warn("Outbox message ID: {} failed on attempt {}/{}. Error: {}",
                    outboxMessage.getId(), outboxMessage.getRetryAttempts(), maxRetryAttempts, e.getMessage());
            handleMessageFailure(outboxMessage, e.getMessage());
        } finally {
            outboxRepository.save(outboxMessage); // Save the updated message (success, failed, or permanently failed)
        }
    }

    private void handleMessageFailure(NotificationOutbox outboxMessage, String error) {
        if (outboxMessage.getRetryAttempts() >= maxRetryAttempts) {
            outboxMessage.markPermanentFailure(error);
            log.error("Outbox outboxMessage ID: {} permanently failed after {} retries. Marked as PERMANENTLY_FAILED.",
                    outboxMessage.getId(), outboxMessage.getRetryAttempts());
            return;
        }

        // Calculate next retry time using exponential backoff
        Instant nextAttempt = getNextAttemptTime(outboxMessage.getRetryAttempts());
        outboxMessage.markAsFailed(error, nextAttempt);
        log.warn("Outbox outboxMessage ID: {} will be re-attempted at {}. Marked as FAILED.",
                outboxMessage.getId(), nextAttempt);
    }

    @Override
    @Transactional
    public int deleteOldProcessedRecords(int retentionDays) {
        Instant cutoffDate = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        int deletedCount = outboxRepository.deleteOldProcessedRecords(
                NotificationStatus.SENT,
                NotificationStatus.PERMANENT_FAILURE,
                cutoffDate
        );
        log.info("Deleted {} old outbox records (SENT/PERMANENT_FAILURE) older than {} days",
                deletedCount, retentionDays);
        return deletedCount;
    }

    public Instant getNextAttemptTime(int retryAttempts) {
        long delayMinutes = (long) (initialRetryIntervalMinutes * Math.pow(
                retryIntervalFactor, retryAttempts));
        return Instant.now().plus(delayMinutes, ChronoUnit.MINUTES);
    }
}
