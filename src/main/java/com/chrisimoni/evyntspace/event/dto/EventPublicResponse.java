package com.chrisimoni.evyntspace.event.dto;

import com.chrisimoni.evyntspace.event.enums.EventStatus;
import com.chrisimoni.evyntspace.event.enums.EventType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EventPublicResponse(
        UUID id,
        String title,
        String summary,
        String description,
        String slug,
        EventType eventType,
        PhysicalEventDetails physicalEventDetails,
        BigDecimal price,
        String eventImageUrl,
        Instant startDate,
        Instant endDate,
        List<AgendaDetails> agendas,
        OrganizerDetails organizer
) {
}
