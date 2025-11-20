package com.chrisimoni.evyntspace.user.service.oauth2;

import com.chrisimoni.evyntspace.user.dto.OAuth2UserInfo;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuth2UserInfoExtractor implements OAuth2UserInfoExtractor {

    @Override
    public OAuth2UserInfo extractUserInfo(OAuth2User oAuth2User) {
        String locale = oAuth2User.getAttribute("locale");

        return OAuth2UserInfo.builder()
                .email(oAuth2User.getAttribute("email"))
                .firstName(oAuth2User.getAttribute("given_name"))
                .lastName(oAuth2User.getAttribute("family_name"))
                .profileImageUrl(oAuth2User.getAttribute("picture"))
                .countryCode(extractCountryCodeFromLocale(locale))
                .build();
    }

    @Override
    public boolean supports(String registrationId) {
        return "google".equalsIgnoreCase(registrationId);
    }

    private String extractCountryCodeFromLocale(String locale) {
        // Google locale format is typically "en-US", "fr-FR", etc.
        if (locale != null && locale.contains("-")) {
            String[] parts = locale.split("-");
            if (parts.length == 2) {
                return parts[1].toUpperCase();
            }
        }
        // Default to US if locale is not available or invalid
        return "US";
    }
}
