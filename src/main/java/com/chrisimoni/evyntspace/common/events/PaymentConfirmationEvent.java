package com.chrisimoni.evyntspace.common.events;

import com.chrisimoni.evyntspace.event.enums.PaymentStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class PaymentConfirmationEvent extends ApplicationEvent {
    private final String reservationNumber;
    private final PaymentStatus status;
    private final UUID transactionId;

    public PaymentConfirmationEvent(Object source, String reservationNumber, PaymentStatus status, UUID transactionId) {
        super(source);
        this.reservationNumber = reservationNumber;
        this.status = status;
        this.transactionId = transactionId;
    }
}
