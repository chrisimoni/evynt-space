package com.chrisimoni.evyntspace.event.service;

import com.chrisimoni.evyntspace.event.dto.ConfirmationDetails;
import com.chrisimoni.evyntspace.event.enums.PaymentStatus;

import java.util.UUID;

public interface EnrollmentService {
    ConfirmationDetails createReservation(UUID eventId, String firstName, String lastName, String email);
    void updateReservationStatus(String reservationNumber, PaymentStatus status, String paymentReference);
    //Optional<Enrollment> findByEventIdAndUserEmail(UUID eventId, String userEmail);
}
