package com.chrisimoni.evyntspace.notification.repository;

import com.chrisimoni.evyntspace.notification.enums.NotificationStatus;
import com.chrisimoni.evyntspace.notification.model.NotificationOutbox;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, UUID> {
    @Query("""
        SELECT no FROM NotificationOutbox no
        WHERE no.status = :failedStatus AND no.nextAttemptTime <= CURRENT_TIMESTAMP
        ORDER BY no.nextAttemptTime ASC, no.createdAt ASC
    """)
    List<NotificationOutbox> findFailedMessagesToProcess(
            @Param("failedStatus") NotificationStatus failedStatus,
            Pageable pageable);

    //When pending is introduced later on
    // Alternative query if nextAttemptTime is NULL for initial PENDING items (process immediately)
    // Find PENDING (with next_attempt_time is null or <= now) OR FAILED (with next_attempt_time <= now)
//    @Query("""
//        SELECT no FROM NotificationOutbox no
//        WHERE (no.status = :pendingStatus AND (no.nextAttemptTime IS NULL OR no.nextAttemptTime <= :now))
//           OR (no.status = :failedStatus AND no.nextAttemptTime <= :now)
//        ORDER BY no.nextAttemptTime ASC, no.createdAt ASC
//    """)
//    List<NotificationOutbox> findMessagesToProcess(
//            @Param("pendingStatus") NotificationStatus pendingStatus,
//            @Param("failedStatus") NotificationStatus failedStatus,
//            @Param("now") Instant now,
//            Pageable pageable);

    /**
     * Deletes old SENT and PERMANENT_FAILURE records to prevent table growth.
     * Only retains records from the last N days for audit purposes.
     */
    @Modifying
    @Query("""
        DELETE FROM NotificationOutbox no
        WHERE (no.status = :sentStatus OR no.status = :permanentFailureStatus)
        AND no.updatedAt < :cutoffDate
    """)
    int deleteOldProcessedRecords(
            @Param("sentStatus") NotificationStatus sentStatus,
            @Param("permanentFailureStatus") NotificationStatus permanentFailureStatus,
            @Param("cutoffDate") Instant cutoffDate);
}
