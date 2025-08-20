package com.chrisimoni.evyntspace.event.scheduler;

import com.chrisimoni.evyntspace.event.enums.EventStatus;
import com.chrisimoni.evyntspace.event.model.Event;
import com.chrisimoni.evyntspace.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublishingScheduler {
    private final EventRepository eventRepository;

    private static final int BATCH_SIZE = 500;

    /**
     * This scheduled job runs every minute to check for events that are ready to be published.
     * The cron expression "0 * * * * *" means the job runs at the beginning of every minute.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void publishScheduledEvents() {
        // Use PageRequest to limit the number of records fetched
        Pageable limit = PageRequest.of(0, BATCH_SIZE);

        List<Event> eventsToPublish = eventRepository.findByStatusAndScheduledPublishDateBefore(
                EventStatus.PENDING_PUBLISH, Instant.now(), limit);

        if (!eventsToPublish.isEmpty()) {
            eventsToPublish.forEach(event -> {
                event.setStatus(EventStatus.PUBLISHED);
                event.setPublishedDate(Instant.now());
                event.setScheduledPublishDate(null);
            });
            eventRepository.saveAll(eventsToPublish);
            log.info("Published {} events.", eventsToPublish.size());
        }
    }
}
