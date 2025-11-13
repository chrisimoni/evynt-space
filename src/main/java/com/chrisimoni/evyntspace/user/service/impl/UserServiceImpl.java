package com.chrisimoni.evyntspace.user.service.impl;

import com.chrisimoni.evyntspace.common.config.AuthenticationContext;
import com.chrisimoni.evyntspace.common.dto.PageResponse;
import com.chrisimoni.evyntspace.common.exception.DuplicateResourceException;
import com.chrisimoni.evyntspace.common.service.BaseServiceImpl;
import com.chrisimoni.evyntspace.user.dto.UserResponse;
import com.chrisimoni.evyntspace.user.dto.UserSearchCriteria;
import com.chrisimoni.evyntspace.user.dto.UserUpdateRequest;
import com.chrisimoni.evyntspace.common.enums.Role;
import com.chrisimoni.evyntspace.user.mapper.UserMapper;
import com.chrisimoni.evyntspace.user.model.User;
import com.chrisimoni.evyntspace.user.repository.UserRepository;
import com.chrisimoni.evyntspace.user.repository.UserSpecification;
import com.chrisimoni.evyntspace.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl extends BaseServiceImpl<User, UUID> implements UserService, UserDetailsService {
    private static final String RESOURCE_NAME = "User";
    private final UserRepository repository;
    private final AuthenticationContext authenticationContext;
    private final UserMapper mapper;

    @Value("${cloudinary.default-user-img}")
    private String defaultImage;

    public UserServiceImpl(UserRepository repository, AuthenticationContext authenticationContext, UserMapper mapper) {
        super(repository, RESOURCE_NAME);
        this.repository = repository;
        this.authenticationContext = authenticationContext;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public User createUser(User model) {
        model.setProfileImageUrl(defaultImage);
        return super.save(model);
    }

    @Override
    public void validateEmailIsUnique(String email) {
        Optional<User> existingUser = repository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new DuplicateResourceException(
                    "This email is already registered. Please login or reset your newPassword.");
        }
    }

    @Override
    public PageResponse<UserResponse> getUsers(UserSearchCriteria criteria) {
        UserSpecification spec = new UserSpecification(criteria);
        Pageable pageable = criteria.toPageable();

        Page<User> users = super.findAll(spec, pageable);
        return mapper.toPageResponse(users);
    }

    @Override
    @Transactional
    public UserResponse getUser(UUID id) {
        User user = findById(id);
        authenticationContext.validateUserAccess(id);

        return mapper.toResponseDto(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return getUserByEmail(email);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User user = findById(id);
        authenticationContext.validateUserAccess(id);
        user = mapper.updateUserFromDto(request, user);

        return mapper.toResponseDto(super.save(user));
    }

    @Override
    public User getUserByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
