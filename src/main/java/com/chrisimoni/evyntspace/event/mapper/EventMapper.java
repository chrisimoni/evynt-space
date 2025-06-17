package com.chrisimoni.evyntspace.event.mapper;

import com.chrisimoni.evyntspace.event.dto.EventCreateRequest;
import com.chrisimoni.evyntspace.event.dto.EventResponse;
import com.chrisimoni.evyntspace.event.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface EventMapper {
    Event toModel(EventCreateRequest dto);
    EventResponse toResponseDto(Event event);
}
