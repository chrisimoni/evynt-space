package com.chrisimoni.evyntspace.payment.service.impl;

import com.chrisimoni.evyntspace.common.exception.ExternalServiceException;
import com.chrisimoni.evyntspace.payment.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StripePaymentServiceImpl implements PaymentService {
    @Value("${spring.application.base-url}")
    private String baseUrl;

    public StripePaymentServiceImpl(@Value("${stripe.api.secret-key}") String secretKey) {
        Stripe.apiKey = secretKey; // initialize Stripe SDK
    }

    @Override
    public String createCheckoutSession(
            String reservationNumber, String customerEmail, String eventTitle, BigDecimal amount, String eventImageUrl) {
        try {
            String successUrl = baseUrl + "/checkout/success";
            String cancelUrl = baseUrl + "/checkout/cancel";

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setCustomerEmail(customerEmail)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("usd")
                                    .setUnitAmount(convertAmountToCent(amount))
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName(eventTitle)
                                            .putMetadata("reservationNumber", reservationNumber)
                                            .addImage(eventImageUrl)
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build();

            Session session = Session.create(params);
            return session.getUrl();
        } catch (StripeException e) {
            throw new ExternalServiceException("Failed to create Stripe checkout session", e);
        }
    }

    private long convertAmountToCent(BigDecimal amount) {
        // Multiply by 100 to get the value in cents
        BigDecimal priceInCents = amount.multiply(new BigDecimal("100"));
        // Convert to a long integer
        return priceInCents.longValueExact();
    }
}
