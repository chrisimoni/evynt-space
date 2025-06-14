package com.chrisimoni.evyntspace.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Still useful to omit null 'errors' list if not present
public class ErrorApiResponse {
    private String status;
    private String message;
    private String code; // e.g., "BAD_REQUEST", "NOT_FOUND", "INTERNAL_SERVER_ERROR"
    private List<ApiErrorDetail> errors; // For validation errors

    public static ErrorApiResponse create(String code, String message) {
        return ErrorApiResponse.builder()
                .status("error")
                .code(code)
                .message(message)
                .build();
    }

    public static ErrorApiResponse create(String code, String message, List<ApiErrorDetail> errors) {
        return ErrorApiResponse.builder()
                .status("error")
                .code(code)
                .message(message)
                .errors(errors)
                .build();
    }
}
