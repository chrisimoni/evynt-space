package com.chrisimoni.evyntspace.event.repository;

import com.chrisimoni.evyntspace.event.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    Optional<Enrollment> findByEventIdAndEmail(UUID eventId, String email);
    Optional<Enrollment> findByReservationNumber(String reservationNumber);
}
