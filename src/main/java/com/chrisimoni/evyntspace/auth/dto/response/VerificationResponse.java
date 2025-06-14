package com.chrisimoni.evyntspace.auth.dto.response;

import java.util.UUID;

public record VerificationResponse(
        UUID verificationToken, // This token links to VerifiedEmailSession
        int expiresInMinutes // How long the token is valid for user creation
) {}
