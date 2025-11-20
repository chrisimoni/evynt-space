package com.chrisimoni.evyntspace.user.service;

import com.chrisimoni.evyntspace.common.enums.AuthProvider;
import com.chrisimoni.evyntspace.common.enums.Role;
import com.chrisimoni.evyntspace.user.dto.CustomOAuth2User;
import com.chrisimoni.evyntspace.user.dto.OAuth2UserInfo;
import com.chrisimoni.evyntspace.user.model.User;
import com.chrisimoni.evyntspace.user.service.oauth2.OAuth2UserInfoExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserService userService;
    private final List<OAuth2UserInfoExtractor> attributeExtractors;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        return processOAuth2User(registrationId, oAuth2User);
    }

    private OAuth2User processOAuth2User(String registrationId, OAuth2User oAuth2User) {
        // Find the appropriate extractor for this OAuth2 provider
        OAuth2UserInfoExtractor extractor = attributeExtractors.stream()
                .filter(e -> e.supports(registrationId))
                .findFirst()
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        "Unsupported OAuth2 provider: " + registrationId));

        // Extract user info using provider-specific logic
        OAuth2UserInfo userInfo = extractor.extractUserInfo(oAuth2User);

        log.info("Processing OAuth2 user from provider: {}, email: {}", registrationId, userInfo.email());

        // Find or create user
        User user;
        try {
            user = userService.getUserByEmail(userInfo.email());
            log.info("Found existing user: {}", user.getEmail());
        } catch (UsernameNotFoundException e) {
            user = createNewUser(userInfo, registrationId);
            user = userService.save(user);
            log.info("Created new user from OAuth2: {}", user.getEmail());
        }

        return new CustomOAuth2User(oAuth2User, user);
    }

    private User createNewUser(OAuth2UserInfo userInfo, String provider) {
        User user = new User();
        user.setEmail(userInfo.email());
        user.setFirstName(userInfo.firstName());
        user.setLastName(userInfo.lastName());
        user.setProfileImageUrl(userInfo.profileImageUrl());
        user.setCountryCode(userInfo.countryCode());
        user.setRole(Role.USER);
        user.setProvider(AuthProvider.valueOf(provider.toUpperCase()));
        user.setActive(true);
        return user;
    }
}
