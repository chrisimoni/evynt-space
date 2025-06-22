package com.chrisimoni.evyntspace.event.dto;

import java.util.UUID;

public record OrganizerDetails(
        UUID id,
        String firstName,
        String lastName,
        String company,
        String profileImgUrl
) {
}
