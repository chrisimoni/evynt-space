package com.chrisimoni.evyntspace.event.repository;

import com.chrisimoni.evyntspace.event.enums.EventStatus;
import com.chrisimoni.evyntspace.event.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {
    boolean existsByTitleIgnoreCase(String title);
    Optional<Event> findBySlugAndStatusAndActiveTrue(String slug, EventStatus eventStatus);
}
