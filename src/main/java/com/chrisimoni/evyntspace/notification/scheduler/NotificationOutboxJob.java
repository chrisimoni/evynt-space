package com.chrisimoni.evyntspace.notification.scheduler;

import com.chrisimoni.evyntspace.notification.enums.NotificationStatus;
import com.chrisimoni.evyntspace.notification.model.NotificationOutbox;
import com.chrisimoni.evyntspace.notification.service.outbox.NotificationOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationOutboxJob {
    private final NotificationOutboxService outboxService;

    @Value("${notification.outbox.processing-batch-size}")
    private int processingBatchSize;

    @Value("${notification.outbox.cleanup-retention-days:7}")
    private int retentionDays;

    @Scheduled(cron = "${notification.outbox.processing-cron-expression}")
    protected void processOutboxMessages() {
        log.info("NotificationOutboxJob running at {}. Looking for messages to process...", Instant.now());

        // Fetch messages ready for processing (NotificationOutboxService handles logic)
        List<NotificationOutbox> messagesToProcess = outboxService.findFailedMessagesToProcess(
                NotificationStatus.FAILED,
                PageRequest.of(0, processingBatchSize)
        );

        if (messagesToProcess.isEmpty()) {
            return;
        }

        log.info("Found {} outbox messages to process.", messagesToProcess.size());
        for (NotificationOutbox message : messagesToProcess) {
            try {
                // Delegate processing of each message to the outboxService.
                // This method is @Transactional(REQUIRES_NEW), so each message is processed atomically.
                outboxService.processSingleOutboxMessage(message);
            } catch (Exception e) {
                // Log and continue if a single message processing fails within its own transaction.
                // The status update for the message (e.g., permanent failure) should happen inside
                // processSingleOutboxMessage. This catch is for unexpected errors preventing that.
                log.error("An unexpected error occurred while attempting to process outbox message ID {}: {}",
                        message.getId(), e.getMessage(), e);
            }
        }
        log.info("NotificationOutboxJob finished processing batch.");
    }

    /**
     * Runs daily at 2 AM to clean up old SENT and PERMANENT_FAILURE records.
     * This prevents the outbox table from growing indefinitely.
     */
    @Scheduled(cron = "${notification.outbox.cleanup-cron-expression:0 0 2 * * *}")
    protected void cleanupOldOutboxRecords() {
        log.info("NotificationOutboxCleanupJob starting at {}. Will delete records older than {} days.",
                Instant.now(), retentionDays);

        try {
            int deletedCount = outboxService.deleteOldProcessedRecords(retentionDays);
            log.info("NotificationOutboxCleanupJob completed. Deleted {} old records.", deletedCount);
        } catch (Exception e) {
            log.error("Error during NotificationOutboxCleanupJob execution: {}", e.getMessage(), e);
        }
    }
}
