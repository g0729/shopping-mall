package com.shopping.main.domain.user.service;

import java.util.Objects;
import java.util.UUID;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.shopping.main.domain.user.constant.UserRole;
import com.shopping.main.domain.user.dto.CustomUserDetails;
import com.shopping.main.domain.user.dto.oauth.GoogleUserInfo;
import com.shopping.main.domain.user.dto.oauth.KakaoUserInfo;
import com.shopping.main.domain.user.dto.oauth.OAuth2UserInfo;
import com.shopping.main.domain.user.entity.SiteUser;
import com.shopping.main.domain.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2Service extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo oAuth2UserInfo = null;

        if (registrationId.equals("google")) {
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else if (registrationId.equals("kakao")) {
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 로그인 서비스입니다.");
        }
        SiteUser user = saveOrLink(oAuth2UserInfo);

        return new CustomUserDetails(user, oAuth2User.getAttributes());
    }

    @Transactional
    private SiteUser saveOrLink(OAuth2UserInfo userInfo) {
        if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_email"),
                    "소셜 계정 이메일을 가져올 수 없습니다.");
        }
        return userRepository.findByEmail(userInfo.getEmail()).map(
                entity -> {

                    String existingProvider = entity.getProvider();
                    if (existingProvider == null || "local".equals(existingProvider)) {
                        return entity.linkSocial(userInfo.getProvider(), userInfo.getProviderId());
                    }

                    // 다른 소셜로 이미 가입된 경우 차단
                    if (!Objects.equals(existingProvider, userInfo.getProvider())) {
                        throw new OAuth2AuthenticationException(
                                new OAuth2Error("duplicate_provider"),
                                "이미 " + existingProvider + " 계정으로 가입되어 있습니다.");
                    }
                    // 같은 소셜로 재로그인
                    return entity.linkSocial(userInfo.getProvider(), userInfo.getProviderId());
                }).orElseGet(() -> {
                    // Case 2 : 신규가입
                    return userRepository.save(
                            SiteUser.builder()
                                    .email(userInfo.getEmail())
                                    .nickname(userInfo.getName())
                                    .password(UUID.randomUUID().toString())
                                    .provider(userInfo.getProvider())
                                    .providerId(userInfo.getProviderId())
                                    .role(UserRole.USER)
                                    .build());
                });
    }
}
