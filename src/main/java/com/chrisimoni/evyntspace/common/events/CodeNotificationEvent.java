package com.chrisimoni.evyntspace.common.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public abstract class CodeNotificationEvent extends ApplicationEvent {
    private final String recipient;
    private final String verificationCode;
    private final int codeValidityInMinutes;

    protected CodeNotificationEvent(Object source, String recipient, String verificationCode, int codeValidityInMinutes) {
        super(source);
        this.recipient = recipient;
        this.verificationCode = verificationCode;
        this.codeValidityInMinutes = codeValidityInMinutes;
    }
}
