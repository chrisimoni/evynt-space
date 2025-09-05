package com.chrisimoni.evyntspace.user.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class VerificationCodeRequestedEvent extends ApplicationEvent {
    private final String recipient;
    private final String verificationCode;
    private final int codeValidityInMinutes;

    public VerificationCodeRequestedEvent(Object source, String recipient, String verificationCode, int codeValidityInMinutes) {
        super(source);
        this.recipient = recipient;
        this.verificationCode = verificationCode;
        this.codeValidityInMinutes = codeValidityInMinutes;
    }
}
