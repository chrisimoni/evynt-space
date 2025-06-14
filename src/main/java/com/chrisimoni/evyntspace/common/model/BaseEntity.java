package com.chrisimoni.evyntspace.common.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public class BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false) // Ensures ID is not updatable and not null
    private UUID id;

    // Automatically sets the timestamp when the entity is first persisted
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Automatically updates the timestamp every time the entity is updated
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Override equals and hashCode for proper entity comparison, especially with UUIDs
    // Lombok's @EqualsAndHashCode(callSuper = false) can generate these,
    // but often manual implementation based on 'id' is safer for JPA entities.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.id); // Entities are equal if their IDs are equal
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0; // Use ID for hash code
    }
}
