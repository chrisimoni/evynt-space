package com.chrisimoni.evyntspace.user.service.oauth2;

import com.chrisimoni.evyntspace.user.dto.OAuth2UserInfo;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2UserInfoExtractor {
    OAuth2UserInfo extractUserInfo(OAuth2User oAuth2User);
    boolean supports(String registrationId);
}
