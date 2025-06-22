package com.chrisimoni.evyntspace.user.dto;

import java.time.Instant;
import java.util.UUID;

public record VerificationResponse(
        UUID verificationToken, // This token links to VerifiedEmailSession
        Instant ExpirationTime // How long the token is valid for user creation
) {}
