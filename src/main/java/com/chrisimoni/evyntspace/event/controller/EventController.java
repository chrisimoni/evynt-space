package com.chrisimoni.evyntspace.event.controller;

import com.chrisimoni.evyntspace.common.dto.ApiResponse;
import com.chrisimoni.evyntspace.common.dto.PageResponse;
import com.chrisimoni.evyntspace.event.dto.*;
import com.chrisimoni.evyntspace.event.mapper.EventMapper;
import com.chrisimoni.evyntspace.event.model.Event;
import com.chrisimoni.evyntspace.event.service.EventService;
import com.chrisimoni.evyntspace.user.dto.UserUpdateRequest;
import com.chrisimoni.evyntspace.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService service;
    private final EventMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EventResponse> createEvent(@Valid @RequestBody EventCreateRequest request) {
        //TODO: to be replaced by Auth userId later
        UUID userId = request.organizerId();
        Event event = service.createEvent(mapper.toModel(request), userId);
        EventResponse response = mapper.toResponseDto(event);
        return ApiResponse.success("Event created successfully", response);
    }

    @GetMapping
    public ApiResponse<PageResponse<EventResponse>> getEvents(@Valid EventSearchCriteria filter) {
        //Modified to include authenticated user
        Page<Event> events = service.findAllEvents(filter, false);
        return ApiResponse.success("Event list retrieved.", mapper.toPageResponse(events));
    }

    @GetMapping("/{id}")
    public ApiResponse<EventResponse> getEvent(@PathVariable("id") UUID id) {
        Event event = service.findById(id);
        return ApiResponse.success("Event retrieved.", mapper.toResponseDto(event));
    }

    @PatchMapping("/{id}")
    public ApiResponse<EventResponse> updateEvent(
            @PathVariable("id") UUID id,
            @Valid @RequestBody EventUpdateRequest request) {
        Event previousEvent = service.findById(id);
        Event eventToUpdate = mapper.updateEventFromDto(request, previousEvent);
        Event updatedEvent = service.updateEvent(eventToUpdate, previousEvent);
        return ApiResponse.success("Event updated.", mapper.toResponseDto(updatedEvent));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<EventResponse> deleteEvent(@PathVariable("id") UUID id) {
        service.updateStatus(id, false);
        return ApiResponse.success("Event deleted");
    }

    //PUBLIC APIs
    @GetMapping("/public-events")
    public ApiResponse<PageResponse<EventPublicResponse>> getEventsForPublic(@Valid EventSearchCriteria filter) {
        Page<Event> events = service.findAllEvents(filter, true);
        return ApiResponse.success("Event list retrieved.", mapper.toPagePublicResponse(events));
    }

    @GetMapping("/public-events/slug/{slug}")
    public ApiResponse<EventPublicResponse> getEventForPublicBySlug(@PathVariable("slug") String slug) {
        Event event = service.findBySlug(slug);
        return ApiResponse.success("Event retrieved.", mapper.toPublicResponseDto(event));
    }

}
