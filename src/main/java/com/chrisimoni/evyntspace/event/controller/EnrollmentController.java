package com.chrisimoni.evyntspace.event.controller;

import com.chrisimoni.evyntspace.common.dto.ApiResponse;
import com.chrisimoni.evyntspace.event.dto.ConfirmationDetails;
import com.chrisimoni.evyntspace.event.dto.EnrollmentRequest;
import com.chrisimoni.evyntspace.event.enums.PaymentStatus;
import com.chrisimoni.evyntspace.event.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @PostMapping("/enroll")
    public ResponseEntity<ApiResponse<ConfirmationDetails>> enrollInEvent(@RequestBody EnrollmentRequest request) {
        ConfirmationDetails response = enrollmentService.createReservation(
                request.eventId(), request.firstName(), request.lastName(), request.email()
        );

        if (response.status() == PaymentStatus.PENDING_PAYMENT) {
            return new ResponseEntity<>(
                    ApiResponse.success("Your order is being processed.", response),
                    HttpStatus.ACCEPTED
            );
        }

        return new ResponseEntity<>(
                ApiResponse.success("Your order is confirmed!", response),
                HttpStatus.OK
        );
    }
}
