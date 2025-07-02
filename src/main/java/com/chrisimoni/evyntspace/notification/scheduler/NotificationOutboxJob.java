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

    //@Scheduled(cron = "${notification.outbox.processing-cron-expression}")
    @Scheduled(cron = "*/10 * * * * *")
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
}
