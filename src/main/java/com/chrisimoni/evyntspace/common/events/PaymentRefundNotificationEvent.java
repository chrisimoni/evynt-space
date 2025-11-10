package com.chrisimoni.evyntspace.common.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class PaymentRefundNotificationEvent extends ApplicationEvent {
    private final String email;
    private final String firstName;
    private final String eventTitle;
    private final BigDecimal amount;

    public PaymentRefundNotificationEvent(
            Object source, String email, String firstName, String eventTitle, BigDecimal amount) {
        super(source);
        this.email = email;
        this.firstName = firstName;
        this.eventTitle = eventTitle;
        this.amount = amount;
    }
}
