package com.chrisimoni.evyntspace.event.service.impl;

import com.chrisimoni.evyntspace.common.exception.BadRequestException;
import com.chrisimoni.evyntspace.common.exception.DuplicateResourceException;
import com.chrisimoni.evyntspace.common.exception.EventSoldOutException;
import com.chrisimoni.evyntspace.event.dto.ConfirmationDetails;
import com.chrisimoni.evyntspace.event.enums.EventStatus;
import com.chrisimoni.evyntspace.event.enums.EventType;
import com.chrisimoni.evyntspace.event.enums.PaymentStatus;
import com.chrisimoni.evyntspace.event.events.PaymentRefundEvent;
import com.chrisimoni.evyntspace.event.events.ReservationConfirmationEvent;
import com.chrisimoni.evyntspace.event.model.Enrollment;
import com.chrisimoni.evyntspace.event.model.Event;
import com.chrisimoni.evyntspace.event.model.PhysicalEventDetails;
import com.chrisimoni.evyntspace.event.repository.EnrollmentRepository;
import com.chrisimoni.evyntspace.event.service.EnrollmentService;
import com.chrisimoni.evyntspace.event.service.EventService;
import com.chrisimoni.evyntspace.event.events.PaymentRefundNotificationEvent;
import com.chrisimoni.evyntspace.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.chrisimoni.evyntspace.common.util.ValidationUtil.validateEmailFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentServiceImpl implements EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final EventService eventService;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public ConfirmationDetails createReservation(UUID eventId, String firstName, String lastName, String email) {
        validateEmailFormat(email);
        Event event = eventService.findById(eventId);

        if(EventStatus.ARCHIVED.equals(event.getStatus())) {
            throw new BadRequestException("The event has already concluded.");
        }

        if (event.getRegistrationCloseDate().isBefore(Instant.now())) {
            throw new BadRequestException("Registration is closed for this event.");
        }

        Enrollment existingEnrollment = enrollmentRepository
                .findByEventIdAndEmail(event.getId(), email)
                .orElse(null);
        if (existingEnrollment != null && PaymentStatus.CONFIRMED.equals(existingEnrollment.getPaymentStatus())) {
            throw new DuplicateResourceException("This email is already enrolled in this event.");
        }

        if (event.getNumberOfSlots() <= 0) {
            throw new EventSoldOutException("No slots available for this event.");
        }

        // Check if the event is a paid event and handle it separately
        if (event.isPaid()) {
            return handlePaidEvent(event, firstName, lastName, email, existingEnrollment);
        }

        // All subsequent logic is for free events
        return handleFreeEvent(event, firstName, lastName, email);
    }

    @Override
    @Transactional
    public void updateReservationStatus(String reservationNumber, PaymentStatus status, UUID transactionId) {
        Optional<Enrollment> optionalEnrollment = enrollmentRepository.findByReservationNumber(reservationNumber);
        if (optionalEnrollment.isEmpty()) {
            // Log an error, this is an unexpected state
            log.error("Received webhook for non-existent or already processed reservation: {}", reservationNumber);
            return;
        }

        Enrollment enrollment = optionalEnrollment.get();
        enrollment.setTransactionId(transactionId);

        switch(status) {
            case CONFIRMED -> handleConfirmedPayment(enrollment);
            case REFUNDED -> handlePaymentRefund(enrollment);
            case CANCELED, FAILED -> {
                enrollment.setPaymentStatus(status);
                enrollmentRepository.save(enrollment);
            }
        }
    }

    private void handlePaymentRefund(Enrollment enrollment) {
        enrollment.setPaymentStatus(PaymentStatus.REFUNDED);
        enrollmentRepository.save(enrollment);

        Event event = eventService.findById(enrollment.getEventId());

        eventPublisher.publishEvent(new PaymentRefundNotificationEvent(
                this,
                enrollment.getEmail(),
                enrollment.getFirstName(),
                event.getTitle(),
                event.getPrice()));
    }

    private void handleConfirmedPayment(Enrollment enrollment) {
        Event event = eventService.findById(enrollment.getEventId());
        int updatedRows = eventService.decrementSlotIfAvailable(enrollment.getEventId());

        // Handle the race condition: payment succeeded, but the slot is gone
        if (updatedRows == 0) {
            log.warn("Payment succeeded but the last slot was just taken. Initiating refund for reservation: {}",
                    enrollment.getReservationNumber());

            eventPublisher.publishEvent(new PaymentRefundEvent(
                    this,
                    event.getOrganizer().getId(),
                    enrollment.getTransactionId()));
            return;
        }

        // Normal successful flow: update status and send notification
        enrollment.setPaymentStatus(PaymentStatus.CONFIRMED);
        enrollmentRepository.save(enrollment);

        triggerConfirmationNotificationEvent(
                enrollment.getReservationNumber(),
                enrollment.getEmail(),
                enrollment.getFirstName(),
                enrollment.getLastName(),
                event
        );
    }

    private ConfirmationDetails handlePaidEvent(
            Event event, String firstName, String lastName, String email, Enrollment existingEnrollment) {

        Enrollment enrollment;

        if (existingEnrollment != null) {
            // Reuse the existing enrollment record (for retries)
            enrollment = existingEnrollment;
            enrollment.setPaymentStatus(PaymentStatus.PENDING_PAYMENT); // Reset status for the new attempt
            enrollment.setFirstName(firstName); // Update name in case it changed
            enrollment.setLastName(lastName);
            // Note: The reservationNumber is preserved
            log.info("Reusing existing enrollment {} for payment retry.", enrollment.getReservationNumber());
        } else {
            // Create a brand new enrollment record
            enrollment = new Enrollment(event.getId(), firstName, lastName, email);
            enrollment.setPaymentStatus(PaymentStatus.PENDING_PAYMENT);
        }

        enrollmentRepository.save(enrollment);

        String checkoutUrl = paymentService.createCheckoutSession(
                event.getOrganizer().getId(),
                enrollment.getReservationNumber(),
                email, event.getTitle(),
                event.getPrice(),
                event.getEventImageUrl());

        return createConfirmationDetails(enrollment, checkoutUrl);
    }

    private ConfirmationDetails handleFreeEvent(Event event, String firstName, String lastName, String email) {
        // Atomically decrement the slot for free events immediately
        int updatedSlots = eventService.decrementSlotIfAvailable(event.getId());
        if (updatedSlots == 0) {
            throw new EventSoldOutException("No slots available for this event.");
        }

        Enrollment enrollment = new Enrollment(event.getId(), firstName, lastName, email);
        enrollment.setPaymentStatus(PaymentStatus.CONFIRMED);
        enrollmentRepository.save(enrollment);

        triggerConfirmationNotificationEvent(enrollment.getReservationNumber(), email, firstName, lastName, event);

        return createConfirmationDetails(enrollment, null);
    }

    private void triggerConfirmationNotificationEvent(String reservationNumber, String email, String firstName, String lastName,
                                          Event event) {
        String venue = (event.getEventType() == EventType.PHYSICAL) ?
                event.getPhysicalEventDetails().getVenueName() : null;

        String fullAddress = (event.getEventType() == EventType.PHYSICAL) ?
                createFullAddress(event.getPhysicalEventDetails()) : null;

        String meetingLink = (event.getEventType() == EventType.ONLINE) ?
                event.getOnlineEventDetails().getMeetingLink() : null;

        String organizerName = event.getOrganizer().getFirstName() + " " + event.getOrganizer().getLastName();
        String organizerEmail = event.getOrganizer().getEmail();

        ReservationConfirmationEvent reservationConfirmationEvent = new ReservationConfirmationEvent(
                this,
                reservationNumber,
                email,
                firstName,
                lastName,
                event.getTitle(),
                event.getSummary(),
                event.getEventType().name(),
                event.isPaid(),
                event.getPrice(),
                event.getEventImageUrl(),
                venue,
                fullAddress,
                meetingLink,
                organizerName,
                organizerEmail
        );

        eventPublisher.publishEvent(reservationConfirmationEvent);
    }

    private String createFullAddress(PhysicalEventDetails details) {
        return String.format("%s, %s, %s, %s",
                details.getAddress(),
                details.getCity(),
                details.getState(),
                details.getCountry());
    }

    private ConfirmationDetails createConfirmationDetails(Enrollment enrollment, String checkoutUrl) {
        return new ConfirmationDetails(
                enrollment.getReservationNumber(),
                enrollment.getEmail(),
                enrollment.getPaymentStatus(),
                checkoutUrl
                );
    }
}
