package com.chrisimoni.evyntspace.user.controller;

import com.chrisimoni.evyntspace.common.dto.ApiResponse;
import com.chrisimoni.evyntspace.common.dto.PageResponse;
import com.chrisimoni.evyntspace.user.dto.UserResponse;
import com.chrisimoni.evyntspace.user.dto.UserSearchCriteria;
import com.chrisimoni.evyntspace.user.dto.UserStatusUpdateRequest;
import com.chrisimoni.evyntspace.user.dto.UserUpdateRequest;
import com.chrisimoni.evyntspace.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> getUsers(@Valid @ParameterObject UserSearchCriteria filter) {
        PageResponse<UserResponse> response = service.getUsers(filter);
        return ApiResponse.success("User list retrieved.", response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable("id") UUID id) {
        UserResponse response = service.getUser(id);
        return ApiResponse.success("User retrieved.", response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PatchMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = service.updateUser(id, request);
        return ApiResponse.success("User updated.", response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        service.updateStatus(id, request.status());
        return ApiResponse.success("User status updated");
    }
}
