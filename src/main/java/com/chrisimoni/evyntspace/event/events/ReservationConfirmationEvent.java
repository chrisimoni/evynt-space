package com.chrisimoni.evyntspace.event.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class ReservationConfirmationEvent extends ApplicationEvent {
    private final String reservationNumber;
    private final String email;
    private final String firstName;
    private final String lastName;

    private final String eventTitle;
    private final String eventSummary;
    private final String eventType;
    private final boolean isPaid;
    private final BigDecimal price;
    private final String eventImageUrl;
    private final String venueName;
    private final String venueAddress;
    private final String meetingLink;

    private final String organizer;
    private final String organizerEmail;

    public ReservationConfirmationEvent(Object source, String reservationNumber, String email,
                                        String firstName, String lastName, String eventTitle,
                                        String eventSummary, String eventType, boolean isPaid, BigDecimal price,
                                        String eventImageUrl, String venueName, String venueAddress,
                                        String meetingLink, String organizer, String organizerEmail) {
        super(source);
        this.reservationNumber = reservationNumber;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.eventTitle = eventTitle;
        this.eventSummary = eventSummary;
        this.eventType = eventType;
        this.isPaid = isPaid;
        this.price = price;
        this.eventImageUrl = eventImageUrl;
        this.venueName = venueName;
        this.venueAddress = venueAddress;
        this.meetingLink = meetingLink;
        this.organizer = organizer;
        this.organizerEmail = organizerEmail;
    }
}
