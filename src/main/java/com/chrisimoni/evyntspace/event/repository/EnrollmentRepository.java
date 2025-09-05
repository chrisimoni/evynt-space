package com.chrisimoni.evyntspace.event.repository;

import com.chrisimoni.evyntspace.event.enums.PaymentStatus;
import com.chrisimoni.evyntspace.event.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    Optional<Enrollment> findByEventIdAndEmailAndPaymentStatus(UUID eventId, String email, PaymentStatus status);
    Optional<Enrollment> findByReservationNumber(String reservationNumber);
}
