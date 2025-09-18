package com.chrisimoni.evyntspace.event.mapper;

import com.chrisimoni.evyntspace.common.dto.PageResponse;
import com.chrisimoni.evyntspace.event.dto.EventCreateRequest;
import com.chrisimoni.evyntspace.event.dto.EventPublicResponse;
import com.chrisimoni.evyntspace.event.dto.EventResponse;
import com.chrisimoni.evyntspace.event.dto.EventUpdateRequest;
import com.chrisimoni.evyntspace.event.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "paid", source = "isPaid", defaultValue = "false")
    Event toModel(EventCreateRequest dto);
    EventResponse toResponseDto(Event event);

    @Mapping(source = "number", target = "pageNumber")
    @Mapping(source = "size", target = "pageSize")
    @Mapping(source = "last", target = "isLast")
    PageResponse<EventResponse> toPageResponse(Page<Event> eventPage);

    EventPublicResponse toPublicResponseDto(Event event);

    @Mapping(source = "number", target = "pageNumber")
    @Mapping(source = "size", target = "pageSize")
    @Mapping(source = "last", target = "isLast")
    PageResponse<EventPublicResponse> toPagePublicResponse(Page<Event> eventPage);

    void toUpdatedEvent(EventUpdateRequest request, @MappingTarget Event eventToUpdate);

    Event copy(Event event);

    // This method takes the update request and the original (unchanged) event.
    // It returns a *new* Event object that represents the updated state.
    default Event updateEventFromDto(EventUpdateRequest request, Event previousEvent) {
        // Step A: Create a mutable copy of the original event.
        // 'eventToUpdate' is a NEW object with 'previousEvent's initial data.
        Event eventToUpdate = copy(previousEvent);

        // Step B: Apply updates from the request DTO to this COPY.
        // The 'updateEventFromDto' method mutates 'eventToUpdate'.
        // The @Condition methods will compare 'request' fields against 'eventToUpdate's current fields
        // (which are effectively 'previousEvent's fields at this point).
        toUpdatedEvent(request, eventToUpdate);

        // Step C: Return the modified copy.
        return eventToUpdate;
    }
}
