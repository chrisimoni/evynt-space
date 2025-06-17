package com.chrisimoni.evyntspace.event.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = EventCreateRequestValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEventCreateRequest {
    String message() default "Invalid event details for the specified event type.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
