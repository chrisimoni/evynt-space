package com.chrisimoni.evyntspace.payment.service;

import com.chrisimoni.evyntspace.payment.dto.StripeOnboardingResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {
    String createCheckoutSession(
            String reservationNumber, String customerEmail, String eventTitle, BigDecimal amount, String eventImageUrl);
    void handleStripeWebhook(String payload, String sigHeader);
    void initiateRefund(UUID transactionId);
    StripeOnboardingResponse createAndOnboardStripeAccount(UUID userId);
    void handleStripeConnectAccountWebhook(String payload, String sigHeader);
}