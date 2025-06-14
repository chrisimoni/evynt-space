package com.chrisimoni.evyntspace.common.exception;

import com.chrisimoni.evyntspace.common.dto.ApiErrorDetail;
import com.chrisimoni.evyntspace.common.dto.ErrorApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    // Handles Spring's MethodArgumentNotValidException (e.g., @Valid on @RequestBody) -> 400 Bad Request
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorApiResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        List<ApiErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> ApiErrorDetail.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        return ErrorApiResponse.create(
                HttpStatus.BAD_REQUEST.name(),
                "One or more validation errors occurred.",
                errors);
    }

    // Handles ResourceNotFoundException -> 404 Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorApiResponse handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        return ErrorApiResponse.create(HttpStatus.NOT_FOUND.name(), ex.getMessage());
    }

    // Handles ValidationException -> 400 Bad Request
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorApiResponse handleValidationException(BadRequestException ex, WebRequest request) {
        return ErrorApiResponse.create(HttpStatus.BAD_REQUEST.name(), ex.getMessage());
    }

    // Handles DuplicateResourceException -> 409 Conflict
    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorApiResponse handleDuplicateResourceException(DuplicateResourceException ex, WebRequest request) {
        return ErrorApiResponse.create(HttpStatus.CONFLICT.name(), ex.getMessage());
    }

    @ExceptionHandler(ExternalServiceException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorApiResponse handleExternalServiceException(ExternalServiceException ex, WebRequest request) {
        return ErrorApiResponse.create(HttpStatus.BAD_GATEWAY.name(), ex.getMessage());
    }

    // Catch-all for any other unexpected exceptions -> 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorApiResponse handleGlobalException(Exception ex, WebRequest request) {
        log.error("An unhandled error occurred: {}", ex.getMessage(), ex);
        return ErrorApiResponse.create(
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "An unexpected error occurred. Please try again later.");
    }
}
