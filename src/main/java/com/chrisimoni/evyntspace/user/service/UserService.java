package com.chrisimoni.evyntspace.user.service;

import com.chrisimoni.evyntspace.common.dto.PageResponse;
import com.chrisimoni.evyntspace.common.service.BaseService;
import com.chrisimoni.evyntspace.user.dto.UserResponse;
import com.chrisimoni.evyntspace.user.dto.UserSearchCriteria;
import com.chrisimoni.evyntspace.user.dto.UserUpdateRequest;
import com.chrisimoni.evyntspace.user.model.User;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface UserService extends BaseService<User, UUID> {
    User createUser(User model);
    User getUserByEmail(String email);
    void validateEmailIsUnique(String email);
    PageResponse<UserResponse> getUsers(UserSearchCriteria filter);
    UserResponse getUser(UUID userId);
    UserDetails loadUserByUsername(String email);
    UserResponse updateUser(UUID id, UserUpdateRequest request);
}
