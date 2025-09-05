package com.chrisimoni.evyntspace.payment.events;

import com.chrisimoni.evyntspace.event.enums.PaymentStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentConfirmationEvent extends ApplicationEvent {
    private final String reservationNumber;
    private final PaymentStatus status;
    private final String paymentReference;

    public PaymentConfirmationEvent(Object source, String reservationNumber, PaymentStatus status, String paymentReference) {
        super(source);
        this.reservationNumber = reservationNumber;
        this.status = status;
        this.paymentReference = paymentReference;
    }
}
