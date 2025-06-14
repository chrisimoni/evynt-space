package com.chrisimoni.evyntspace.user.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserStatusUpdateRequest(
        @NotNull(message = "Status cannot be null")
        Boolean status // (true for active, false for inactive)
) {
}
