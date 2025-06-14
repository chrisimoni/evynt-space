package com.chrisimoni.evyntspace.notification.repository;

import com.chrisimoni.evyntspace.notification.model.NotificationOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, UUID> {
}
