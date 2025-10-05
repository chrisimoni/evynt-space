package com.chrisimoni.evyntspace.event.model;

import com.chrisimoni.evyntspace.common.model.BaseEntity;
import com.chrisimoni.evyntspace.event.enums.PaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "enrollments")
public class Enrollment extends BaseEntity {
    private UUID eventId;
    private String firstName;
    private String lastName;
    private String email;
    private String reservationNumber;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    // This is the explicit Foreign Key column (UUID) to the 'transactions' table.
    // There is NO @OneToOne JPA annotation here to enforce separation.
    private UUID transactionId;

    public Enrollment(UUID eventId, String firstName, String lastName, String email) {
        this.eventId = eventId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        // A simple, non-production way to generate a unique order number.
        this.reservationNumber = "#" + System.currentTimeMillis();
    }
}
