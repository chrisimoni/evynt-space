package com.chrisimoni.evyntspace.user.service.impl;

import com.chrisimoni.evyntspace.common.enums.Role;
import com.chrisimoni.evyntspace.common.events.LoginCodeNotificationEvent;
import com.chrisimoni.evyntspace.common.events.PasswordResetNotificationEvent;
import com.chrisimoni.evyntspace.common.events.VerificationCodeRequestedEvent;
import com.chrisimoni.evyntspace.common.exception.BadRequestException;
import com.chrisimoni.evyntspace.common.exception.InvalidPasswordException;
import com.chrisimoni.evyntspace.common.exception.InvalidTokenException;
import com.chrisimoni.evyntspace.common.exception.UserDisabledException;
import com.chrisimoni.evyntspace.user.dto.AuthRequest;
import com.chrisimoni.evyntspace.user.dto.AuthResponse;
import com.chrisimoni.evyntspace.user.dto.ChangePasswordRequest;
import com.chrisimoni.evyntspace.user.dto.UserCreateRequest;
import com.chrisimoni.evyntspace.user.mapper.UserMapper;
import com.chrisimoni.evyntspace.user.model.Token;
import com.chrisimoni.evyntspace.user.model.User;
import com.chrisimoni.evyntspace.user.model.VerificationCode;
import com.chrisimoni.evyntspace.user.model.VerifiedSession;
import com.chrisimoni.evyntspace.user.repository.VerificationCodeRepository;
import com.chrisimoni.evyntspace.user.repository.VerificationSessionRepository;
import com.chrisimoni.evyntspace.user.service.AuthService;
import com.chrisimoni.evyntspace.user.service.JwtService;
import com.chrisimoni.evyntspace.user.service.TokenService;
import com.chrisimoni.evyntspace.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

import static com.chrisimoni.evyntspace.common.util.ValidationUtil.*;
import static com.chrisimoni.evyntspace.user.util.UserUtil.hash;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final VerificationCodeRepository verificationCodeRepository;
    private final VerificationSessionRepository sessionRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Value("${auth.code-validity}")
    int codeValidity;
    @Value("${auth.session-validity}")
    int sessionValidity;

    @Value("${auth.refresh-token-validity}")
    private int refreshTokenValidity;
    @Value("${auth.password-reset-token-validity}")
    private int passwordResetTokenValidity;

    @Value("${app.frontend.url}")
    String frontendBaseUrl;

    @Override
    @Transactional
    public void requestVerificationCode(String email) {
        validateEmailFormat(email);
        userService.validateEmailIsUnique(email);
        verificationCodeRepository.invalidatePreviousCodes(email);

        String code = generateAndSaveCode(email);

        eventPublisher.publishEvent(new VerificationCodeRequestedEvent(
                this, email, code, codeValidity));
    }

    @Override
    @Transactional
    public VerifiedSession confirmVerificationCode(String email, String code) {
        validateEmailFormat(email);
        VerificationCode verificationCode = verificationCodeRepository
                .findActiveVerificatonCodeByEmail(email, Instant.now())
                .orElseThrow(() -> new BadRequestException("Verification failed: No active code found or code expired/used."));

        if(!Objects.equals(hash(code), verificationCode.getCode())) {
            throw new BadRequestException("Verification failed: Invalid code.");
        }

        verificationCode.setUsed(true);
        verificationCodeRepository.save(verificationCode);

        return createSession(email);
    }

    @Transactional
    protected VerifiedSession createSession(String email) {
        //TODO: rework verification session
        sessionRepository.invalidatePreviousSessions(email);
        Instant sessionExpirationTime = Instant.now().plus(sessionValidity, ChronoUnit.MINUTES);
        VerifiedSession session = new VerifiedSession(email, sessionExpirationTime);
        return sessionRepository.save(session);
    }

    @Override
    @Transactional
    public void verifyEmailSession(String email, UUID verficationToken) {
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

    @Override
    @Transactional
    public AuthResponse signup(UserCreateRequest request) {
        validate(request);
        verifyEmailSession(request.email(), request.verificationToken());

        User user = userMapper.toEnity(request);
        user.setCountryCode(user.getCountryCode().toUpperCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);

        user = userService.createUser(user);

        return authResponse(user);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        User user = userService.getUserByEmail(request.email());
        checkUserIsActive(user);

        // Authenticate user credentials using Spring Security's mechanism
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        if(!authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Invalid credentials");
        }

        return authResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshTokenString) {
        Token refreshToken = tokenService.verifyToken(refreshTokenString);
        if (!refreshToken.isRefreshToken()) {
            throw new InvalidTokenException("Not a refresh token");
        }

        User user = refreshToken.getUser();
        checkUserIsActive(user);

        return authResponse(user);
    }

    private void checkUserIsActive(User user) {
        if (!user.isEnabled()) {
            throw new UserDisabledException("User account is disabled");
        }
    }

    @Override
    public void requestPasswordReset(String email) {
        User user = userService.getUserByEmail(email);
        String plainToken = tokenService.createPasswordResetToken(user, refreshTokenValidity);
        String resetUrl = frontendBaseUrl + "/reset-newPassword?token=" + plainToken;

        eventPublisher.publishEvent(new PasswordResetNotificationEvent(
                this, email, resetUrl, passwordResetTokenValidity));
    }

    @Override
    @Transactional
    public void resetPassword(String tokenString, String newPassword) {
        Token token = tokenService.verifyToken(tokenString);
        if (!token.isPasswordResetToken()) {
            throw new InvalidTokenException("Not a newPassword reset token");
        }

        // Update newPassword
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);

        // Delete token immediately after use
        tokenService.deleteToken(token);

        log.info("Password reset successful for user: {}", user.getEmail());

    }

    // Logout - delete refresh token
    @Transactional
    public void logout(String refreshTokenString) {
        // Delete refresh token
        tokenService.deleteToken(refreshTokenString);
    }

    @Override
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userService.findById(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password is not correct.");
        }

        // Optional: Prevent reuse of the same password
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new InvalidPasswordException("New password cannot be the same as the current password.");
        }

        String newHashedPassword = passwordEncoder.encode(request.newPassword());
        user.setPassword(newHashedPassword);
        userService.save(user);
    }

    @Override
    @Transactional
    public void requestLoginCode(String email) {
        validateEmailFormat(email);
        User user = userService.getUserByEmail(email);
        checkUserIsActive(user);

        String plainCode = tokenService.createLoginToken(user, codeValidity);

        eventPublisher.publishEvent(new LoginCodeNotificationEvent(
                this, email, plainCode, codeValidity));

    }

    @Override
    public AuthResponse verifyAndGenerateToken(String email, String code) {
        User user = userService.getUserByEmail(email);

        Token loginToken = tokenService.verifyToken(code);
        if (!loginToken.isLoginToken()) {
            throw new InvalidTokenException("Not a login token");
        }

        String tokenEmail = loginToken.getUser().getEmail();

        if(!tokenEmail.equalsIgnoreCase(email)) {
            throw new BadRequestException("Mismatched email during token validation.");
        }

        return authResponse(user);
    }

    public String generateAndSaveCode(String email) {
        //generate 6-digit code
        String plainCode = generateCode();
        Instant expirationTime = Instant.now().plus(codeValidity, ChronoUnit.MINUTES);
        VerificationCode verificationCode = new VerificationCode(email, hash(plainCode), expirationTime);
        verificationCodeRepository.save(verificationCode);

        return plainCode;
    }

    private String generateCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }

    protected void validate(UserCreateRequest request) {
        validateEmailFormat(request.email());
        userService.validateEmailIsUnique(request.email());
        validatePassword(request.password());
        validateCountryCode(request.countryCode());
    }

    private AuthResponse authResponse(User user) {
        String accessToken = jwtService.generateToken(user);
        String refreshToken = tokenService.createRefreshToken(user, refreshTokenValidity);

        return new AuthResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                accessToken,
                refreshToken);
    }
}
