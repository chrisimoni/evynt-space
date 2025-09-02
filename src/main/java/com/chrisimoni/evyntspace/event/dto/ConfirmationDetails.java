package com.chrisimoni.evyntspace.event.dto;

import com.chrisimoni.evyntspace.event.enums.PaymentStatus;

public record ConfirmationDetails(
        String reservationNumber,
        String email,
        PaymentStatus status,
        String checkoutUrl
) {}
