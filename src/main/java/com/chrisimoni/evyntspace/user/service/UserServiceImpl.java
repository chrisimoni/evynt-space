package com.chrisimoni.evyntspace.user.service;

import com.chrisimoni.evyntspace.common.exception.DuplicateResourceException;
import com.chrisimoni.evyntspace.common.service.BaseServiceImpl;
import com.chrisimoni.evyntspace.user.dto.UserSearchCriteria;
import com.chrisimoni.evyntspace.user.model.User;
import com.chrisimoni.evyntspace.user.repository.UserRepository;
import com.chrisimoni.evyntspace.user.repository.UserSpecification;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl extends BaseServiceImpl<User, UUID> implements UserService {
    private static final String RESOURCE_NAME = "User";
    private final UserRepository repository;

    @Value("${cloudinary.default-user-img}")
    private String defaultImage;

    public UserServiceImpl(UserRepository repository) {
        super(repository, RESOURCE_NAME);
        this.repository = repository;
    }

    @Override
    @Transactional
    public User createUser(User model) {
        model.setProfileImageUrl(defaultImage);
        return super.save(model);
    }

    @Override
    public void validateEmailIsUnique(String email) {
        Optional<User> existingUser = getUserByEmail(email);
        if (existingUser.isPresent()) {
            throw new DuplicateResourceException(
                    "This email is already registered. Please login or reset your password.");
        }
    }

    @Override
    public Page<User> findAllUsers(UserSearchCriteria criteria) {
        UserSpecification spec = new UserSpecification(criteria);
        Pageable pageable = criteria.toPageable();

        return super.findAll(spec, pageable);
    }

    private Optional<User> getUserByEmail(String email) {
        return repository.findByEmail(email);
    }
}
