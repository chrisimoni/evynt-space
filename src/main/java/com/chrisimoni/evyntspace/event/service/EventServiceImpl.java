package com.chrisimoni.evyntspace.event.service;

import com.chrisimoni.evyntspace.common.exception.DuplicateResourceException;
import com.chrisimoni.evyntspace.common.service.BaseServiceImpl;
import com.chrisimoni.evyntspace.event.enums.EventStatus;
import com.chrisimoni.evyntspace.event.model.Event;
import com.chrisimoni.evyntspace.event.repository.EventRepository;
import com.chrisimoni.evyntspace.user.model.User;
import com.chrisimoni.evyntspace.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

import static com.chrisimoni.evyntspace.event.util.EventUtil.generateSlug;

@Service
public class EventServiceImpl extends BaseServiceImpl<Event, UUID> implements EventService {
    private static final String RESOURCE_NAME = "Event";
    private final EventRepository repository;
    private final UserService userService;

    @Value("${cloudinary.default-img-url}")
    private String defaultImage;

    public EventServiceImpl(EventRepository repository, UserService userService) {
        super(repository, RESOURCE_NAME);
        this.repository = repository;
        this.userService = userService;
    }

    @Override
    public Event createEvent(Event event, UUID userId) {
        if(repository.existsByTitleIgnoreCase(event.getTitle())) {
            throw new DuplicateResourceException("An event with the same title already exists.");
        }

        User organizer = userService.findById(userId);
        event.setOrganizer(organizer);
        event.setSlug(generateSlug(event.getTitle()));
        event.setEventImageUrl(Objects.nonNull(event.getEventImageUrl()) ? event.getEventImageUrl() : defaultImage);

        if(Objects.nonNull(event.getScheduledPublishDate())) {
            event.setStatus(EventStatus.PENDING_PUBLISH);
            event.setPublishedDate(null);
        }

        return super.save(event);
    }
}
