package com.chrisimoni.evyntspace.user.dto;

import lombok.Builder;

@Builder
public record OAuth2UserInfo(
        String email,
        String firstName,
        String lastName,
        String profileImageUrl,
        String countryCode
) {
}
