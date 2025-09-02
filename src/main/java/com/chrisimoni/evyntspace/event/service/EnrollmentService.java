package com.chrisimoni.evyntspace.event.service;

import com.chrisimoni.evyntspace.event.dto.ConfirmationDetails;

import java.util.UUID;

public interface EnrollmentService {
    ConfirmationDetails createReservation(UUID eventId, String firstName, String lastName, String email);
    //Optional<Enrollment> findByEventIdAndUserEmail(UUID eventId, String userEmail);
}
