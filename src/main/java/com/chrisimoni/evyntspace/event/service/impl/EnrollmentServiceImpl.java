package com.chrisimoni.evyntspace.event.service.impl;

import com.chrisimoni.evyntspace.common.exception.DuplicateResourceException;
import com.chrisimoni.evyntspace.common.exception.EventSoldOutException;
import com.chrisimoni.evyntspace.event.dto.ConfirmationDetails;
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
import com.chrisimoni.evyntspace.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

        Optional<Enrollment> existingEnrollment = enrollmentRepository
                .findByEventIdAndEmailAndPaymentStatus(event.getId(), email, PaymentStatus.CONFIRMED);
        if (existingEnrollment.isPresent()) {
            throw new DuplicateResourceException("This email is already enrolled in this event.");
        }

        if (event.getNumberOfSlots() <= 0) {
            throw new EventSoldOutException("No slots available for this event.");
        }

        // Check if the event is a paid event and handle it separately
        if (event.getPrice() != null && event.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            return handlePaidEvent(event, firstName, lastName, email);
        }

        // All subsequent logic is for free events
        return handleFreeEvent(event, firstName, lastName, email);
    }

    @Override
    @Transactional
    public void updateReservationStatus(String reservationNumber, PaymentStatus status, String paymentReference) {
        Optional<Enrollment> optionalEnrollment = enrollmentRepository.findByReservationNumber(reservationNumber);
        if (optionalEnrollment.isEmpty()) {
            // Log an error, this is an unexpected state
            log.error("Received webhook for non-existent or already processed reservation: {}", reservationNumber);
            return;
        }

        Enrollment enrollment = optionalEnrollment.get();
        enrollment.setPaymentReference(paymentReference);
        
        if (PaymentStatus.CONFIRMED.equals(status)) {
            handleConfirmedPayment(enrollment);
            return;
        }

        // Handle all other statuses (CANCELED, FAILED)
        enrollment.setPaymentStatus(status);
        enrollmentRepository.save(enrollment);
    }

    private void handleConfirmedPayment(Enrollment enrollment) {
        Event event = eventService.findById(enrollment.getEventId());
        int updatedRows = eventService.decrementSlotIfAvailable(enrollment.getEventId());

        // Handle the race condition: payment succeeded, but the slot is gone
        if (updatedRows == 0) {
            log.warn("Payment succeeded but the last slot was just taken. Initiating refund for reservation: {}",
                    enrollment.getReservationNumber());
            enrollment.setPaymentStatus(PaymentStatus.REFUNDED);
            enrollmentRepository.save(enrollment);
            paymentService.initiateRefund(enrollment.getPaymentReference());
            eventPublisher.publishEvent(new PaymentRefundEvent(
                    this, enrollment.getEmail(), enrollment.getFirstName(), event.getTitle(), event.getPrice()));
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

    private ConfirmationDetails handlePaidEvent(Event event, String firstName, String lastName, String email) {
        Enrollment enrollment = new Enrollment(event.getId(), firstName, lastName, email);
        enrollment.setPaymentStatus(PaymentStatus.PENDING_PAYMENT);
        enrollmentRepository.save(enrollment);

        String checkoutUrl = paymentService.createCheckoutSession(
                enrollment.getReservationNumber(), email, event.getTitle(), event.getPrice(), event.getEventImageUrl());

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
