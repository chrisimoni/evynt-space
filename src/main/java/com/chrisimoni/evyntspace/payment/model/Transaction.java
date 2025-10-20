package com.chrisimoni.evyntspace.payment.model;

import com.chrisimoni.evyntspace.common.model.BaseEntity;
import com.chrisimoni.evyntspace.payment.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction extends BaseEntity {
    // --- Core Financial Link ---
    private String paymentReferenceId;

    // --- Financial Details ---
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount; // Gross amount paid by customer
    private String currency; // e.g., "USD"

    // --- Transaction Status ---
    @Enumerated(EnumType.STRING)
    private TransactionStatus status; // SUCCEEDED, FAILED, CANCELED, REFUNDED

    private String accountId;
}
