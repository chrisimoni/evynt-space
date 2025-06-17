package com.chrisimoni.evyntspace.event.repository;

import com.chrisimoni.evyntspace.event.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    boolean existsByTitleIgnoreCase(String title);
}
