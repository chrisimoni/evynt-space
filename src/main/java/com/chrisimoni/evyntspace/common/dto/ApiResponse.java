package com.chrisimoni.evyntspace.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Exclude null fields from JSON output
public class ApiResponse<T> {
    private String status; // "success" or "error"
    private String message;
    private T data; // Generic type for actual data

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .status("success")
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .message(message)
                .data(data)
                .build();
    }
}
