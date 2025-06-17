package com.chrisimoni.evyntspace.event.model;


import com.chrisimoni.evyntspace.common.model.ActivatableEntity;
import com.chrisimoni.evyntspace.event.enums.EventStatus;
import com.chrisimoni.evyntspace.event.enums.EventType;
import com.chrisimoni.evyntspace.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class Event extends ActivatableEntity {
    private String title;
    @Column(nullable = false)
    private String summary;
    @Column(columnDefinition = "TEXT")
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    @Embedded
    private PhysicalEventDetails physicalEventDetails; // Will be null for online events
    @Embedded
    private OnlineEventDetails onlineEventDetails; // Will be null for physical events

    @Column(nullable = false)
    private Integer numberOfSlots;
    BigDecimal price;
    private String eventImageUrl;
    private String slug;

    private Instant startDate;
    private Instant endDate;
    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.PUBLISHED;
    private Instant publishedDate = Instant.now();
    private Instant scheduledPublishDate;
}
