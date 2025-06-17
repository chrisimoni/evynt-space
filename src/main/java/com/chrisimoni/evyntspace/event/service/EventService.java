package com.chrisimoni.evyntspace.event.service;

import com.chrisimoni.evyntspace.common.service.BaseService;
import com.chrisimoni.evyntspace.event.model.Event;

import java.util.UUID;

public interface EventService extends BaseService<Event, UUID> {
    Event createEvent(Event event, UUID userId);
}
