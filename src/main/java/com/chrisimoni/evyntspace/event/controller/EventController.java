package com.chrisimoni.evyntspace.event.controller;

import com.chrisimoni.evyntspace.common.dto.ApiResponse;
import com.chrisimoni.evyntspace.common.dto.PageResponse;
import com.chrisimoni.evyntspace.event.dto.*;
import com.chrisimoni.evyntspace.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService service;

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EventResponse> createEvent(@Valid @RequestBody EventCreateRequest request) {
        EventResponse response = service.createEvent(request);
        return ApiResponse.success("Event created successfully", response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    public ApiResponse<PageResponse<EventResponse>> getEvents(@Valid @ParameterObject EventSearchCriteria filter) {
        PageResponse<EventResponse> response = service.getEvents(filter);
        return ApiResponse.success("Event list retrieved.", response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}")
    public ApiResponse<EventResponse> getEvent(@PathVariable("id") UUID id) {
        EventResponse response = service.getEvent(id);
        return ApiResponse.success("Event retrieved.", response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PatchMapping("/{id}")
    public ApiResponse<EventResponse> updateEvent(
            @PathVariable("id") UUID id,
            @Valid @RequestBody EventUpdateRequest request) {
        EventResponse response = service.updateEvent(id, request);
        return ApiResponse.success("Event updated.", response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @DeleteMapping("/{id}")
    public ApiResponse<EventResponse> deleteEvent(@PathVariable("id") UUID id) {
        service.deleteEvent(id);
        return ApiResponse.success("Event deleted");
    }

    //PUBLIC APIs
    @GetMapping("/public-events")
    public ApiResponse<PageResponse<EventPublicResponse>> getEventsForPublic(@Valid EventSearchCriteria filter) {
        PageResponse<EventPublicResponse> response = service.getPublicEvents(filter);
        return ApiResponse.success("Event list retrieved.", response);
    }

    @GetMapping("/public-events/slug/{slug}")
    public ApiResponse<com.chrisimoni.evyntspace.event.dto.EventPublicResponse> getEventForPublicBySlug(@PathVariable("slug") String slug) {
        EventPublicResponse response = service.getEventBySlug(slug);
        return ApiResponse.success("Event retrieved.", response);
    }

}
