package com.chrisimoni.evyntspace.event.service.impl;

import com.chrisimoni.evyntspace.common.exception.DuplicateResourceException;
import com.chrisimoni.evyntspace.common.exception.EventSoldOutException;
import com.chrisimoni.evyntspace.event.dto.ConfirmationDetails;
import com.chrisimoni.evyntspace.event.enums.EventType;
import com.chrisimoni.evyntspace.event.enums.PaymentStatus;
import com.chrisimoni.evyntspace.event.event.ReservationConfirmationEvent;
import com.chrisimoni.evyntspace.event.model.Enrollment;
import com.chrisimoni.evyntspace.event.model.Event;
import com.chrisimoni.evyntspace.event.model.PhysicalEventDetails;
import com.chrisimoni.evyntspace.event.repository.EnrollmentRepository;
import com.chrisimoni.evyntspace.event.service.EnrollmentService;
import com.chrisimoni.evyntspace.event.service.EventService;
import com.chrisimoni.evyntspace.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static com.chrisimoni.evyntspace.common.util.ValidationUtil.validateEmailFormat;

@Service
@RequiredArgsConstructor
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

        Optional<Enrollment> existingEnrollment = enrollmentRepository.findByEventIdAndEmail(event.getId(), email);
        if (existingEnrollment.isPresent()) {
            throw new DuplicateResourceException("This email is already enrolled in this event.");
        }

        // Check if the event is a paid event and handle it separately
        if (event.getPrice() != null && event.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            return handlePaidEvent(event, firstName, lastName, email);
        }

        // All subsequent logic is for free events
        return handleFreeEvent(event, firstName, lastName, email);
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
