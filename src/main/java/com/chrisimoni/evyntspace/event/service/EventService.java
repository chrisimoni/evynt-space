package com.chrisimoni.evyntspace.event.service;

import com.chrisimoni.evyntspace.common.service.BaseService;
import com.chrisimoni.evyntspace.event.dto.EventSearchCriteria;
import com.chrisimoni.evyntspace.event.model.Event;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface EventService extends BaseService<Event, UUID> {
    Event createEvent(Event event, UUID userId);
    Event updateEvent(Event eventToUpdate, Event previousEvent);
    Page<Event> findAllEvents(EventSearchCriteria searchCriteria, boolean forPublic);
    Event findBySlug(String slug);
}
