package com.chrisimoni.evyntspace.payment.dto;

public record StripeOnboardingResponse(
        String accountId,
        String accountLinkUrl
) {
}
