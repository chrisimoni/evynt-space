package com.chrisimoni.evyntspace.event.scheduler;

import com.chrisimoni.evyntspace.event.enums.EventStatus;
import com.chrisimoni.evyntspace.event.enums.PaymentStatus;
import com.chrisimoni.evyntspace.event.model.Event;
import com.chrisimoni.evyntspace.event.repository.EnrollmentRepository;
import com.chrisimoni.evyntspace.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventScheduler {
    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;

    private static final Pageable PAGE_REQUEST = PageRequest.of(0, 2000);

    /**
     * This scheduled job runs every minute to check for events that are ready to be published.
     * The cron expression "0 * * * * *" means the job runs at the beginning of every minute.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void publishScheduledEvents() {

        List<Event> eventsToPublish = eventRepository.findByStatusAndScheduledPublishDateBefore(
                EventStatus.PENDING_PUBLISH, Instant.now(), PAGE_REQUEST);

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

    @Scheduled(fixedRate = 90000) // Fixed Rate in milliseconds (90 seconds)
    @Transactional
    public void archiveCompletedEvents() {
        List<Event> eventsToArchive = eventRepository.findByEndDateBeforeAndStatusNot(
                Instant.now(), EventStatus.ARCHIVED, PAGE_REQUEST);

        if (!eventsToArchive.isEmpty()) {
            eventsToArchive.forEach(event -> event.setStatus(EventStatus.ARCHIVED));

            eventRepository.saveAll(eventsToArchive);
            log.info("Archived {} completed events.", eventsToArchive.size());
        }
    }

    /**
     * Runs every 30 seconds to expire stale enrollments and restore event slots.
     * Optimized for tables with millions of records.
     */
    @Scheduled(cron = "*/30 * * * * *")
    @Transactional
    public void expireStaleEnrollments() {
        LocalDateTime cutoffLocal = LocalDateTime.now().minusMinutes(5);
        Instant cutoffInstant = cutoffLocal.atZone(ZoneId.systemDefault()).toInstant();
        List<String> excluded = List.of(PaymentStatus.CONFIRMED.name(), PaymentStatus.EXPIRED.name());
        String newStatus = PaymentStatus.EXPIRED.name();

        // Single atomic query
        int affectedEvents = enrollmentRepository.expireStaleEnrollmentsAndRestoreSlots(
                cutoffInstant,
                excluded,
                newStatus
        );

        if(affectedEvents > 0) {
            log.info("Reversed slots for {} events", affectedEvents);
        }

    }
}
