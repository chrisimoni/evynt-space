package com.chrisimoni.evyntspace.common.exception;

import com.chrisimoni.evyntspace.common.dto.ApiErrorDetail;
import com.chrisimoni.evyntspace.common.dto.ErrorApiResponse;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    // Handles Spring's MethodArgumentNotValidException (e.g., @Valid on @RequestBody) -> 400 Bad Request
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorApiResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        List<ApiErrorDetail> errors = new ArrayList<>();

        // 1. Collect Field Errors
        errors.addAll(ex.getBindingResult().getFieldErrors().stream()
                .map(error -> ApiErrorDetail.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .toList());

        // 2. Collect Global Errors (from class-level constraints)
        errors.addAll(ex.getBindingResult().getGlobalErrors().stream()
                .map(error -> ApiErrorDetail.builder()
                        .field(null) // Global errors don't have a specific field
                        .message(error.getDefaultMessage())
                        .build())
                .toList());

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

    @ExceptionHandler(EventSoldOutException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorApiResponse handleEventSoldOut(EventSoldOutException ex) {
        return ErrorApiResponse.create(HttpStatus.CONFLICT.name(), ex.getMessage());
    }

    @ExceptionHandler(ExternalServiceException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorApiResponse handleExternalServiceException(ExternalServiceException ex, WebRequest request) {
        return ErrorApiResponse.create(HttpStatus.BAD_GATEWAY.name(), ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorApiResponse handleMaxSizeException(MaxUploadSizeExceededException ex) {
        log.error(ex.getMessage(), ex);
        String message = String.format("File size exceeds the maximum allowed size of %s.", maxFileSize);
        return ErrorApiResponse.create(HttpStatus.BAD_REQUEST.name(),
                message);
    }

    // Simplified Handler for JSON deserialization errors (e.g., invalid enum values)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorApiResponse handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        String detailMessage; // This will be the message for ApiErrorDetail
        String topLevelMessage; // This will be the main message in ErrorApiResponse
        String fieldName = null;

        Throwable mostSpecificCause = ex.getMostSpecificCause();

        if (mostSpecificCause instanceof InvalidFormatException ife) {
            topLevelMessage = "Validation Error"; // Specific for InvalidFormatException

            // Extract the field name from the path
            List<JsonMappingException.Reference> path = ife.getPath();
            if (path != null && !path.isEmpty()) {
                fieldName = path.get(path.size() - 1).getFieldName();
            }

            // Customize the message for the detail
            detailMessage = String.format("Invalid value '%s' for field '%s'.",
                    ife.getValue(),
                    fieldName != null ? fieldName : "unknown field");

            // Add accepted enum values if applicable
            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                detailMessage += " Accepted values are: " + java.util.Arrays.stream(ife.getTargetType().getEnumConstants())
                        .map(e -> ((Enum<?>) e).name())
                        .collect(Collectors.joining(", "));
            }

        } else {
            topLevelMessage = "Invalid request payload.";
            detailMessage = mostSpecificCause.getMessage();
        }

        List<ApiErrorDetail> errors = Collections.singletonList(
                ApiErrorDetail.builder()
                        .field(fieldName)
                        .message(detailMessage)
                        .build()
        );

        return ErrorApiResponse.create(
                HttpStatus.BAD_REQUEST.name(),
                topLevelMessage, // Use the dynamically set top-level message
                errors);
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
