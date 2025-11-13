package com.chrisimoni.evyntspace.common.config;

import com.chrisimoni.evyntspace.common.enums.Role;
import com.chrisimoni.evyntspace.common.model.SecurityPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Component
public class AuthenticationContext {

    /**
     * Get the full Authentication object
     */
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Get the current authenticated username
     */
    public String getCurrentUsername() {
        Authentication authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        return authentication.getName();
    }

    /**
     * Get the current authenticated user (returns null if not authenticated)
     */
    public String getCurrentUsernameOrNull() {
        Authentication authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        return authentication.getName();
    }

    /**
     * Get current user's authorities/roles
     */
    public Collection<? extends GrantedAuthority> getCurrentUserAuthorities() {
        Authentication authentication = getAuthentication();

        if (authentication == null) {
            return Collections.emptyList();
        }

        return authentication.getAuthorities();
    }

    /**
     * Check if current user has a specific role
     */
    public boolean hasRole(String role) {
        return getCurrentUserAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Check if current user has a specific authority
     */
    public boolean hasAuthority(String authority) {
        return getCurrentUserAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(authority));
    }

    /**
     * Get the UserDetails object (if you're using a custom UserDetails)
     */
    public UserDetails getCurrentUserDetails() {
        Authentication authentication = getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new IllegalStateException("No authenticated user found");
        }

        return (UserDetails) authentication.getPrincipal();
    }

    public UUID getCurrentUserId() {
        Authentication authentication = getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof SecurityPrincipal securityPrincipal) {
            return securityPrincipal.getUserId();
        }

        throw new IllegalStateException("Authenticated principal does not implement SecurityPrincipal.");
    }

    public void validateUserAccess(UUID targetUserId) {
        if (!canAccessUser(targetUserId)) {
            throw new AccessDeniedException("The current user does not have the permissions for the requested operation.");
        }
    }

    private boolean canAccessUser(UUID targetUserId) {
        if (hasRole(Role.ADMIN.name())) {
            return true;
        }
        UUID currentUserId = getCurrentUserId();
        return currentUserId.equals(targetUserId);
    }
}
