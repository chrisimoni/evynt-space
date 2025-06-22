package com.chrisimoni.evyntspace.user.service;

import com.chrisimoni.evyntspace.common.service.BaseService;
import com.chrisimoni.evyntspace.user.dto.UserSearchCriteria;
import com.chrisimoni.evyntspace.user.model.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface UserService extends BaseService<User, UUID> {
    User createUser(User model, UUID token);
    void validateEmailIsUnique(String email);
    Page<User> findAllUsers(UserSearchCriteria filter);
}
