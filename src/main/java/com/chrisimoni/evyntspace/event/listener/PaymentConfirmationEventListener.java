package com.chrisimoni.evyntspace.event.listener;

import com.chrisimoni.evyntspace.event.service.EnrollmentService;
import com.chrisimoni.evyntspace.common.events.PaymentConfirmationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentConfirmationEventListener {
    private final EnrollmentService enrollmentService;

    @EventListener
    @Async
    public void handlePaymentConfirmationEvent(PaymentConfirmationEvent event) {
        log.info("PaymentConfirmationEvent received for {}.", event.getReservationNumber());
        enrollmentService.updateReservationStatus(
                event.getReservationNumber(), event.getStatus(), event.getTransactionId());
    }
}
