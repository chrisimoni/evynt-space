package com.chrisimoni.evyntspace.payment.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {
    String createCheckoutSession(
            String reservationNumber, String customerEmail, String eventTitle, BigDecimal amount, String eventImageUrl);
    void handleStripeWebhook(String payload, String sigHeader);
    void initiateRefund(String paymentIntentId);
}
