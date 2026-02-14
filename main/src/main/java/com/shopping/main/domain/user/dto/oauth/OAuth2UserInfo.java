package com.shopping.main.domain.user.dto.oauth;

public interface OAuth2UserInfo {
    String getProviderId();

    String getProvider();

    String getEmail();

    String getName();
}