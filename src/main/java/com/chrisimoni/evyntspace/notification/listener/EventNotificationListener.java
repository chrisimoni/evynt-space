package com.chrisimoni.evyntspace.notification.listener;

import com.chrisimoni.evyntspace.event.event.ReservationConfirmationEvent;
import com.chrisimoni.evyntspace.notification.enums.MessageTemplate;
import com.chrisimoni.evyntspace.notification.model.MessageDetails;
import com.chrisimoni.evyntspace.notification.service.NotificationContentBuilder;
import com.chrisimoni.evyntspace.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.chrisimoni.evyntspace.notification.util.NotificationUtil.createMapLink;
import static com.chrisimoni.evyntspace.notification.util.NotificationUtil.generateQrCodeDataUrl;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventNotificationListener {
    private final NotificationService notificationService;
    private final NotificationContentBuilder contentBuilder;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleReservationConfirmationEvent(ReservationConfirmationEvent event) {
        log.info("ReservationConfirmationEvent received for {}.", event.getEmail());

        Map<String, Object> templateModel = buildReservationTemplateModel(event);

        MessageDetails messageDetails = contentBuilder.createMessageDetails(
                event.getEmail(), MessageTemplate.RESERVATION_CONFIRMATION_NOTIFICATION, templateModel);

        notificationService.send(messageDetails);
    }

    private Map<String, Object> buildReservationTemplateModel(ReservationConfirmationEvent event) {
        Map<String, Object> templateModel = new HashMap<>();

        // Add event details
        String mapLink = Objects.isNull(event.getVenueAddress()) ? null : createMapLink(event.getVenueAddress());
        templateModel.put("event", new HashMap<String, Object>() {{
            put("title", event.getEventTitle());
            put("summary", event.getEventSummary());
            put("imageUrl", event.getEventImageUrl());
            put("isFree", event.getPrice().doubleValue() == 0.0);
            put("fee", event.getPrice());
            put("type", event.getEventType());
            put("venue", event.getVenueName());
            put("address", event.getVenueAddress());
            put("meetingLink", event.getMeetingLink());
            put("mapLink", mapLink);
        }});

        // Add ticket details
        templateModel.put("ticket", new HashMap<String, String>() {{
            put("reservationNumber", event.getReservationNumber());
        }});

        // Add attendee details
        templateModel.put("attendee", new HashMap<String, String>() {{
            put("name", event.getFirstName() + " " + event.getLastName());
            put("email", event.getEmail());
        }});

        // Add organizer details
        templateModel.put("organizer", new HashMap<String, String>() {{
            put("name", event.getOrganizer());
            put("email", event.getOrganizerEmail());
        }});

        // Generate the QR code URL and add to the model
        String qrCodeUrl = generateQrCodeDataUrl(event.getReservationNumber());
        templateModel.put("qrCodeUrl", qrCodeUrl);

        return templateModel;
    }
}
