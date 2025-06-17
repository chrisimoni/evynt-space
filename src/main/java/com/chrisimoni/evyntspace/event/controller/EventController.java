package com.chrisimoni.evyntspace.event.controller;

import com.chrisimoni.evyntspace.common.dto.ApiResponse;
import com.chrisimoni.evyntspace.event.dto.EventCreateRequest;
import com.chrisimoni.evyntspace.event.dto.EventResponse;
import com.chrisimoni.evyntspace.event.mapper.EventMapper;
import com.chrisimoni.evyntspace.event.model.Event;
import com.chrisimoni.evyntspace.event.service.EventService;
import com.chrisimoni.evyntspace.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService service;
    private final EventMapper mapper;

    @PostMapping
    public ApiResponse<EventResponse> createEvent(@Valid @RequestBody EventCreateRequest request) {
        //TODO: to be replaced by Auth userId later
        UUID userId = request.organizerId();
        Event event = service.createEvent(mapper.toModel(request), userId);
        EventResponse response = mapper.toResponseDto(event);
        return ApiResponse.success("Event created successfully", response);
    }

}
