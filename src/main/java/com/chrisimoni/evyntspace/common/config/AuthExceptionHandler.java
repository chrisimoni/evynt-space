package com.chrisimoni.evyntspace.common.config;

import com.chrisimoni.evyntspace.common.dto.ErrorApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    // --- Implementation for AuthenticationEntryPoint (401 Unauthorized) ---
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        handleError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed: " + authException.getMessage());
    }

    // --- Implementation for AccessDeniedHandler (403 Forbidden) ---
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        handleError(response, HttpServletResponse.SC_FORBIDDEN, "Access Denied: You do not have the necessary permissions.");
    }

    // --- Centralized Error Writing Method ---
    private void handleError(HttpServletResponse response, int status, String message) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        var errorResponse = ErrorApiResponse.create(
                status == 401 ? HttpStatus.UNAUTHORIZED.name() : HttpStatus.FORBIDDEN.name(),
                message);

        response.setContentType("application/json");
        response.setStatus(status);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
