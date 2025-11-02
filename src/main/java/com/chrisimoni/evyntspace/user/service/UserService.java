package com.chrisimoni.evyntspace.user.service;

import com.chrisimoni.evyntspace.common.service.BaseService;
import com.chrisimoni.evyntspace.user.dto.UserSearchCriteria;
import com.chrisimoni.evyntspace.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface UserService extends BaseService<User, UUID> {
    User createUser(User model);
    User getUserByEmail(String email);
    void validateEmailIsUnique(String email);
    Page<User> findAllUsers(UserSearchCriteria filter);
    UserDetails loadUserByUsername(String email);
}
