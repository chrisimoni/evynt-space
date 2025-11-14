package com.chrisimoni.evyntspace.payment.controller;

import com.chrisimoni.evyntspace.common.dto.ApiResponse;
import com.chrisimoni.evyntspace.payment.dto.StripeOnboardingResponse;
import com.chrisimoni.evyntspace.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/stripe/webhook")
    public ResponseEntity<Void> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String sigHeader) {
        paymentService.handleStripeWebhook(payload, sigHeader);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/stripe/connect/webhook")
    public ResponseEntity<Void> handleStripeConnectAccountWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String sigHeader) {
        paymentService.handleStripeConnectAccountWebhook(payload, sigHeader);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping("stripe/connect/onboard")
    public ApiResponse<StripeOnboardingResponse> generateOnboardingLink() {
        return ApiResponse.success("Stripe connect account created successfully.",
                paymentService.createAndOnboardStripeAccount());
    }
}
