package com.chrisimoni.evyntspace.user.controller;

import com.chrisimoni.evyntspace.common.dto.ApiResponse;
import com.chrisimoni.evyntspace.common.dto.PageResponse;
import com.chrisimoni.evyntspace.user.dto.UserResponse;
import com.chrisimoni.evyntspace.user.dto.UserSearchCriteria;
import com.chrisimoni.evyntspace.user.dto.UserStatusUpdateRequest;
import com.chrisimoni.evyntspace.user.dto.UserUpdateRequest;
import com.chrisimoni.evyntspace.user.mapper.UserMapper;
import com.chrisimoni.evyntspace.user.model.User;
import com.chrisimoni.evyntspace.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;
    private final UserMapper mapper;

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> getUsers(@Valid @ParameterObject UserSearchCriteria filter) {
        Page<User> users = service.findAllUsers(filter);
        return ApiResponse.success("User list retrieved.", mapper.toPageResponse(users));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable("id") UUID id) {
        User user = service.findById(id);
        return ApiResponse.success("User retrieved.", mapper.toResponseDto(user));
    }

    @PatchMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UserUpdateRequest request) {
        User userToUpdate = mapper.updateUserFromDto(request, service.findById(id));
        User updatedUser = service.save(userToUpdate);
        return ApiResponse.success("User updated.", mapper.toResponseDto(updatedUser));
    }

    //TODO: this method might not be required
    @GetMapping("/profile")
    public ApiResponse<UserResponse> getUserProfile() {
        //TODO: userId to be retrieved by auth token later
        User user = service.findById(UUID.randomUUID());
        return ApiResponse.success("User retrieved.", mapper.toResponseDto(user));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        service.updateStatus(id, request.status());
        return ApiResponse.success("User status updated");
    }
}
