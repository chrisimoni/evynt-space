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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class Event extends ActivatableEntity {
    private String title;
    private String summary;
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Agenda> agendas;

    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.PUBLISHED;
    private Instant publishedDate = Instant.now();
    private Instant scheduledPublishDate;
}
