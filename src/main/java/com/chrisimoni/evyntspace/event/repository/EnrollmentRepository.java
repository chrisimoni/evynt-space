package com.chrisimoni.evyntspace.event.repository;

import com.chrisimoni.evyntspace.event.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    Optional<Enrollment> findByEventIdAndEmail(UUID eventId, String email);
    Optional<Enrollment> findByReservationNumber(String reservationNumber);

    /**
     * Expires stale enrollments and restores event slots in a single atomic operation.
     * Optimized for millions of records with proper indexing.
     * NOTE: This returns the number of EVENTS affected.
     */
    @Modifying
    @Transactional
    @Query(value = """
        WITH expired_enrollments AS (
            UPDATE enrollments e
            SET payment_status = :newStatus
            FROM events ev
            WHERE e.event_id = ev.id
                AND e.payment_status NOT IN :excludedStatuses
                AND e.updated_at < :cutoffTime
                AND ev.is_paid = TRUE
            RETURNING e.event_id
        ),
        slot_counts AS (
            SELECT event_id, COUNT(*) as expired_count
            FROM expired_enrollments
            GROUP BY event_id
        )
        UPDATE events ev
        SET number_of_slots = ev.number_of_slots + sc.expired_count
        FROM slot_counts sc
        WHERE ev.id = sc.event_id
        """, nativeQuery = true)
    int expireStaleEnrollmentsAndRestoreSlots(
            @Param("cutoffTime") Instant cutoffTime,
            @Param("excludedStatuses") List<String> excludedStatuses,
            @Param("newStatus") String newStatus
    );
}
