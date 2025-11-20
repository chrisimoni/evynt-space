package com.chrisimoni.evyntspace.event.service.impl;

import com.chrisimoni.evyntspace.common.config.AuthenticationContext;
import com.chrisimoni.evyntspace.common.dto.PageResponse;
import com.chrisimoni.evyntspace.common.enums.Role;
import com.chrisimoni.evyntspace.common.exception.BadRequestException;
import com.chrisimoni.evyntspace.common.exception.DuplicateResourceException;
import com.chrisimoni.evyntspace.common.exception.ResourceNotFoundException;
import com.chrisimoni.evyntspace.common.service.BaseServiceImpl;
import com.chrisimoni.evyntspace.event.dto.*;
import com.chrisimoni.evyntspace.event.dto.EventPublicResponse;
import com.chrisimoni.evyntspace.event.enums.EventStatus;
import com.chrisimoni.evyntspace.event.mapper.EventMapper;
import com.chrisimoni.evyntspace.event.model.Event;
import com.chrisimoni.evyntspace.event.repository.EventRepository;
import com.chrisimoni.evyntspace.event.repository.EventSpecification;
import com.chrisimoni.evyntspace.event.service.EventService;
import com.chrisimoni.evyntspace.payment.model.PaymentAccount;
import com.chrisimoni.evyntspace.payment.service.PaymentAccountService;
import com.chrisimoni.evyntspace.user.model.User;
import com.chrisimoni.evyntspace.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.chrisimoni.evyntspace.event.util.EventUtil.generateSlug;
import static com.chrisimoni.evyntspace.event.util.EventUtil.isNullOrEmpty;

@Service
public class EventServiceImpl extends BaseServiceImpl<Event, UUID> implements EventService {
    private static final String RESOURCE_NAME = "Event";
    private final EventRepository repository;
    private final UserService userService;
    private final PaymentAccountService paymentAccountService;
    private final AuthenticationContext authenticationContext;
    private final EventMapper mapper;

    @Value("${cloudinary.default-event-img}")
    private String defaultEventImage;

    @Value("${cloudinary.default-user-img}")
    private String defaultUserImage;

    public EventServiceImpl(
            EventRepository repository, UserService userService, PaymentAccountService paymentAccountService, AuthenticationContext authenticationContext, EventMapper mapper) {
        super(repository, RESOURCE_NAME);
        this.repository = repository;
        this.userService = userService;
        this.paymentAccountService = paymentAccountService;
        this.authenticationContext = authenticationContext;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public EventResponse createEvent(EventCreateRequest request) {
        validateTitle(request.title());
        User organizer = userService.findById(request.organizerId());
        authenticationContext.validateUserAccess(request.organizerId());

        // Validate country is provided for all events
        validateOrganizerProfile(organizer);

        if(request.isPaid()) {
            validateOrganizerPaymentStatus(organizer.getId());
        }

        Event event = mapper.toEntity(request);

        event.setOrganizer(organizer);
        event.setPrice(event.isPaid() ? event.getPrice() : BigDecimal.ZERO);
        event.setSlug(generateSlug(event.getTitle()));
        event.setEventImageUrl(Objects.nonNull(event.getEventImageUrl()) ? event.getEventImageUrl() : defaultEventImage);
        processAgendas(event);

        if(Objects.nonNull(event.getScheduledPublishDate())) {
            event.setStatus(EventStatus.PENDING_PUBLISH);
            event.setPublishedDate(null);
        }

        event = super.save(event);

        return mapper.toResponseDto(event);
    }

    private void validateOrganizerProfile(User organizer) {
        if (organizer.getCountryCode() == null || organizer.getCountryCode().isBlank()) {
            throw new BadRequestException(
                    "Please complete your profile by adding your country before creating events."
            );
        }
    }

    private void validateOrganizerPaymentStatus(UUID userId) {
        Optional<PaymentAccount> account = paymentAccountService.findByUserId(userId);
        if (account.isEmpty()) {
            throw new BadRequestException(
                    "Please set up your Stripe Connect account to create paid events."
            );
        }

        PaymentAccount existingAccount = account.get();
        if (!existingAccount.isChargesEnabled() || !existingAccount.isPayoutsEnabled()) {
            throw new BadRequestException(
                    "Your Stripe Connect account is not fully enabled. Please complete the setup to create paid events."
            );
        }
    }

    @Override
    @Transactional
    public EventResponse updateEvent(UUID id, EventUpdateRequest request) {
        Event previousEvent = findById(id);
        authenticationContext.validateUserAccess(previousEvent.getOrganizer().getId());
        Event eventToUpdate = mapper.updateEventFromDto(request, previousEvent);
        if(!Objects.equals(eventToUpdate.getTitle(), previousEvent.getTitle())) {
            validateTitle(eventToUpdate.getTitle());
            eventToUpdate.setSlug(generateSlug(eventToUpdate.getTitle()));
        }

        validateEventDates(eventToUpdate.getStartDate(), eventToUpdate.getEndDate());
        processAgendas(eventToUpdate);

        //OPTIONAL: check if the start or end date has changed and trigger notification to enrolled users

        super.save(eventToUpdate);

        return mapper.toResponseDto(eventToUpdate);
    }

    @Override
    public EventPublicResponse getEventBySlug(String slug) {
        Event event = repository.findBySlugAndStatusAndActiveTrue(slug, EventStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("No event found"));

        return mapper.toPublicResponseDto(event);
    }

    @Override
    public PageResponse<EventResponse> getEvents(EventSearchCriteria criteria) {
        boolean isAdmin = authenticationContext.hasRole(Role.ADMIN.name());
        if(!isAdmin) {
            UUID currentUserId = authenticationContext.getCurrentUserId();
            criteria.setOrganizerId(currentUserId);
        }

        Page<Event> events = findAllEvents(criteria);
        return mapper.toPageResponse(events);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EventPublicResponse> getPublicEvents(EventSearchCriteria criteria) {
        criteria.setStatus(EventStatus.PUBLISHED);
        criteria.setActive(true);
        Page<Event> events = findAllEvents(criteria);
        return mapper.toPagePublicResponse(events);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEvent(UUID id) {
        Event event = findById(id);
        authenticationContext.validateUserAccess(event.getOrganizer().getId());
        return mapper.toResponseDto(event);
    }

    private Page<Event> findAllEvents(EventSearchCriteria criteria) {
        EventSpecification spec = new EventSpecification(criteria);
        Pageable pageable = criteria.toPageable();

        return super.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteEvent(UUID eventId) {
        Event event = findById(eventId);
        authenticationContext.validateUserAccess(event.getOrganizer().getId());
        updateStatus(eventId, false);
    }

    @Override
    @Transactional
    public int decrementSlotIfAvailable(UUID eventId) {
        return repository.decrementSlotIfAvailable(eventId);
    }

    private void validateEventDates(Instant startDate, Instant endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date.");
        }
    }

    private void validateTitle(String title) {
        if(repository.existsByTitleIgnoreCase(title)) {
            throw new DuplicateResourceException("An event with the same title already exists.");
        }
    }

    private void processAgendas(Event event) {
        if (!isNullOrEmpty(event.getAgendas())) {
            event.getAgendas().forEach(agenda -> {
                if (StringUtils.isEmpty(agenda.getPresenterImageUrl())) {
                    agenda.setPresenterImageUrl(defaultUserImage);
                }
            });
        }
    }
}
