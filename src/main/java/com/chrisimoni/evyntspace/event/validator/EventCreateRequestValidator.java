package com.chrisimoni.evyntspace.event.validator;

import com.chrisimoni.evyntspace.event.dto.EventCreateRequest;
import com.chrisimoni.evyntspace.event.enums.EventType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class EventCreateRequestValidator implements ConstraintValidator<ValidEventCreateRequest, EventCreateRequest> {
    @Override
    public boolean isValid(EventCreateRequest request, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        return Objects.isNull(request)
                || (validateOnlyOneEventDetailsIsProvided(request, context)
                && validateEventDetailsByType(request, context)
                && validateDates(request, context)
                && isValidCountry(request, context));
    }

    private boolean validateOnlyOneEventDetailsIsProvided(EventCreateRequest request, ConstraintValidatorContext context) {
        // Determine if physical or online details are present
        boolean hasPhysical = Objects.nonNull(request.physicalEventDetails());
        boolean hasOnline = Objects.nonNull(request.onlineEventDetails());

        // Cannot provide both physical and online event details
        if (hasPhysical && hasOnline) {
            // Disable the default violation message to provide a more specific one
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Cannot provide both physical and online event details.")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean validateEventDetailsByType(EventCreateRequest request, ConstraintValidatorContext context) {
        if (request.eventType() == EventType.PHYSICAL && Objects.isNull(request.physicalEventDetails())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Physical event details are required for a PHYSICAL event.")
                    .addPropertyNode("physicalEventDetails")
                    .addConstraintViolation();
            return false;
        }

        if (request.eventType() == EventType.ONLINE && Objects.isNull(request.onlineEventDetails())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Online event details are required for an ONLINE event.")
                    .addPropertyNode("onlineEventDetails")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean isValidCountry(EventCreateRequest request, ConstraintValidatorContext context) {
        if(Objects.isNull(request.physicalEventDetails())) {
            return true;
        }

        String country = request.physicalEventDetails().country();

        boolean isValid = Arrays.stream(Locale.getISOCountries())
                .map(code -> Locale.of("", code).getDisplayCountry())
                .anyMatch(displayCountry -> displayCountry.equalsIgnoreCase(country));

        if (!isValid) {
            context.buildConstraintViolationWithTemplate("Country name is not valid.")
                    .addPropertyNode("physicalEventDetails.country")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean validateDates(EventCreateRequest request, ConstraintValidatorContext context) {
        // Check if endDate is before startDate
        if (request.endDate().isBefore(request.startDate())) {
            context.buildConstraintViolationWithTemplate("End date cannot be before start date")
                    .addPropertyNode("endDate")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
