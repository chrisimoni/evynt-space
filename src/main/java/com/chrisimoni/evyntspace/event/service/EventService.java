package com.chrisimoni.evyntspace.event.service;

import com.chrisimoni.evyntspace.common.dto.PageResponse;
import com.chrisimoni.evyntspace.common.service.BaseService;
import com.chrisimoni.evyntspace.event.dto.*;
import com.chrisimoni.evyntspace.event.model.Event;

import java.util.UUID;

public interface EventService extends BaseService<Event, UUID> {
    EventResponse createEvent(EventCreateRequest request);
    EventResponse updateEvent(UUID eventId, EventUpdateRequest request);
    EventPublicResponse getEventBySlug(String slug);
    EventResponse getEvent(UUID eventId);
    int decrementSlotIfAvailable(UUID eventId);
    PageResponse<EventResponse> getEvents(EventSearchCriteria filter);
    PageResponse<EventPublicResponse> getPublicEvents(EventSearchCriteria filter);
    void deleteEvent(UUID eventId);
}
