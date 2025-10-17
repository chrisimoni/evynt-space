package com.chrisimoni.evyntspace.event.service.impl;

import com.chrisimoni.evyntspace.common.exception.BadRequestException;
import com.chrisimoni.evyntspace.common.exception.DuplicateResourceException;
import com.chrisimoni.evyntspace.common.exception.ResourceNotFoundException;
import com.chrisimoni.evyntspace.common.service.BaseServiceImpl;
import com.chrisimoni.evyntspace.event.dto.EventSearchCriteria;
import com.chrisimoni.evyntspace.event.enums.EventStatus;
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

    @Value("${cloudinary.default-event-img}")
    private String defaultEventImage;

    @Value("${cloudinary.default-user-img}")
    private String defaultUserImage;

    public EventServiceImpl(
            EventRepository repository, UserService userService, PaymentAccountService paymentAccountService) {
        super(repository, RESOURCE_NAME);
        this.repository = repository;
        this.userService = userService;
        this.paymentAccountService = paymentAccountService;
    }

    @Override
    @Transactional
    public Event createEvent(Event event, UUID userId) {
        validateTitle(event.getTitle());

        User organizer = userService.findById(userId);

        if(event.isPaid()) {
            validateOrganizerPaymentStatus(organizer.getId());
        }

        event.setOrganizer(organizer);
        event.setPrice(event.isPaid() ? event.getPrice() : BigDecimal.ZERO);
        event.setSlug(generateSlug(event.getTitle()));
        event.setEventImageUrl(Objects.nonNull(event.getEventImageUrl()) ? event.getEventImageUrl() : defaultEventImage);
        processAgendas(event);

        if(Objects.nonNull(event.getScheduledPublishDate())) {
            event.setStatus(EventStatus.PENDING_PUBLISH);
            event.setPublishedDate(null);
        }

        return super.save(event);
    }

    public void validateOrganizerPaymentStatus(UUID userId) {
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
    public Event updateEvent(Event eventToUpdate, Event previousEvent) {
        if(!Objects.equals(eventToUpdate.getTitle(), previousEvent.getTitle())) {
            validateTitle(eventToUpdate.getTitle());
            eventToUpdate.setSlug(generateSlug(eventToUpdate.getTitle()));
        }

        validateEventDates(eventToUpdate.getStartDate(), eventToUpdate.getEndDate());
        processAgendas(eventToUpdate);

        // Check if the start or end date has changed
        boolean sendNotification = !Objects.equals(eventToUpdate.getStartDate(), previousEvent.getStartDate()) ||
                !Objects.equals(eventToUpdate.getEndDate(), previousEvent.getEndDate());

        super.save(eventToUpdate);

        //TODO: if sendNotification, trigger notifcation to update enrolled users of event date changes

        return eventToUpdate;
    }

    @Override
    public Page<Event> findAllEvents(EventSearchCriteria criteria, boolean forPublic) {
        if(forPublic) {
            criteria.setStatus(EventStatus.PUBLISHED);
            criteria.setActive(true);
        }

        EventSpecification spec = new EventSpecification(criteria);
        Pageable pageable = criteria.toPageable();

        return super.findAll(spec, pageable);
    }

    @Override
    public Event findBySlug(String slug) {
        return repository.findBySlugAndStatusAndActiveTrue(slug, EventStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("No event found"));
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
