package com.chrisimoni.evyntspace.user.service;

import com.chrisimoni.evyntspace.common.exception.BadRequestException;
import com.chrisimoni.evyntspace.common.exception.DuplicateResourceException;
import com.chrisimoni.evyntspace.common.service.BaseServiceImpl;
import com.chrisimoni.evyntspace.user.dto.UserSearchCriteria;
import com.chrisimoni.evyntspace.user.model.User;
import com.chrisimoni.evyntspace.user.model.VerifiedSession;
import com.chrisimoni.evyntspace.user.repository.UserRepository;
import com.chrisimoni.evyntspace.user.repository.UserSpecification;
import com.chrisimoni.evyntspace.user.repository.VerificationSessionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.chrisimoni.evyntspace.common.util.ValidationUtil.validateEmailFormat;
import static com.chrisimoni.evyntspace.common.util.ValidationUtil.validatePassword;

@Service
public class UserServiceImpl extends BaseServiceImpl<User, UUID> implements UserService {
    private static final String RESOURCE_NAME = "User";
    private final UserRepository repository;
    private final VerificationSessionRepository sessionRepository;

    @Value("${cloudinary.default-user-img}")
    private String defaultImage;

    public UserServiceImpl(UserRepository repository, VerificationSessionRepository sessionRepository) {
        super(repository, RESOURCE_NAME);
        this.repository = repository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    @Transactional
    public User createUser(User model, UUID verficationToken) {
        validate(model);
        verifyEmailSession(model.getEmail(), verficationToken);
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

    private void verifyEmailSession(String email, UUID verficationToken) {
        //validate email verification token
        VerifiedSession verifiedSession = sessionRepository
                .findByIdAndIsUsedFalseAndExpirationTimeAfter(verficationToken, Instant.now())
                .orElseThrow(() -> new BadRequestException(
                        "Email verification token is invalid or expired. Please re-verify your email."));

        if(!verifiedSession.getEmail().equalsIgnoreCase(email)) {
            throw new BadRequestException("Email in request does not match verified email in token.");
        }

        //mark the verification session as used to prevent reuse
        verifiedSession.setUsed(true);
        sessionRepository.save(verifiedSession);
    }

    private void validate(User model) {
        validateEmailFormat(model.getEmail());
        validateEmailIsUnique(model.getEmail());
        validatePassword(model.getPassword());
    }
}
