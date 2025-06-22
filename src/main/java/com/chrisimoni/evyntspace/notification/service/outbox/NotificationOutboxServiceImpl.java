package com.chrisimoni.evyntspace.notification.service.outbox;

import com.chrisimoni.evyntspace.notification.enums.NotificationStatus;
import com.chrisimoni.evyntspace.notification.enums.NotificationType;
import com.chrisimoni.evyntspace.notification.model.MessageDetails;
import com.chrisimoni.evyntspace.notification.model.NotificationOutbox;
import com.chrisimoni.evyntspace.notification.repository.NotificationOutboxRepository;
import com.chrisimoni.evyntspace.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationOutboxServiceImpl implements NotificationOutboxService{
    private final NotificationOutboxRepository outboxRepository;
    //private final NotificationService notificationService;

    @Value("${notification.outbox.processing-batch-size}")
    private int processingBatchSize;

    @Value("${notification.outbox.max-retry-attempts}")
    private int maxRetryAttempts;

    @Value("${notification.outbox.initial-retry-interval-minutes}")
    private int initialRetryIntervalMinutes;

    @Value("${notification.outbox.retry-interval-factor}")
    private int retryIntervalFactor;


    @Override
    @Transactional
    public void saveToOutbox(MessageDetails messageDetails, NotificationType type, String error) {
        NotificationOutbox notificationOutbox = new NotificationOutbox(messageDetails, type);
        error = setErrorMessage(error, type);
        long delayMinutes = (long) (initialRetryIntervalMinutes * Math.pow(
                retryIntervalFactor, notificationOutbox.getRetryAttempts()));
        Instant nextAttempt = Instant.now().plus(delayMinutes, ChronoUnit.MINUTES);
        notificationOutbox.markAsFailed(error, nextAttempt);

        outboxRepository.save(notificationOutbox);
    }

    // This method will be scheduled to run periodically
    //@Scheduled(cron = "${notification.outbox.processing-cron-expression}")
//    @Scheduled(cron = "*/5 * * * * *")
//    @Transactional // Each run of the scheduler method is a single transaction
//    public void processOutboxMessages() {
//        log.info("Scheduler running every sec");
//        Instant now = Instant.now();
//
//        // Fetch messages ready for processing
//        // Using the custom query for robust pending/failed selection
//        List<NotificationOutbox> messagesToProcess = outboxRepository.findFailedMessagesToProcess(
//                NotificationStatus.FAILED,
//                PageRequest.of(0, processingBatchSize)
//        );
//
//        if (messagesToProcess.isEmpty()) {
//            return;
//        }
//
//        for (NotificationOutbox message : messagesToProcess) {
//            MessageDetails details = message.getMessageDetails();
//            try {
//                // --- Perform the actual notification sending based on type
//                // we only have Email Impl for now
//                switch (message.getNotificationType()) {
//                    case EMAIL:
//                        notificationService.send(details);
//                        break;
//                    case SMS:
//                        // For this example, let's just log SMS for now
//                        System.out.println("Simulating SMS to " + details.getRecipient() + ": " + details.getBody());
//                        break;
//                    default:
//                        throw new IllegalArgumentException("Unknown notification type: "
//                                + message.getNotificationType());
//                }
//                // --- Mark as SENT on success ---
//                message.markAsSent();
//
//            } catch (Exception e) {
//                // --- Handle failure ---
//                String errorMessage = setErrorMessage(e.getMessage(), message.getNotificationType());
//
//                if (message.getRetryAttempts() >= maxRetryAttempts) {
//                    message.markPermanentFailure(errorMessage);
//                } else {
//                    // Calculate next attempt time with exponential backoff
//                    long delayMinutes = (long) (initialRetryIntervalMinutes * Math.pow(retryIntervalFactor, message.getRetryAttempts()));
//                    Instant nextAttempt = Instant.now().plus(delayMinutes, ChronoUnit.MINUTES);
//                    message.markAsFailed(errorMessage, nextAttempt);
//                }
//            }
//
//        }
//        // Save all changes in a single transaction (due to @Transactional on method)
//        outboxRepository.saveAll(messagesToProcess);
//    }

    private String setErrorMessage(String error, NotificationType notificationType) {
        return Objects.nonNull(error)
                ? error.substring(0, Math.min(error.length(), 255))
                : String.format(
                "Unknown error while sending %s notification",
                notificationType.name().toLowerCase());
    }
}
