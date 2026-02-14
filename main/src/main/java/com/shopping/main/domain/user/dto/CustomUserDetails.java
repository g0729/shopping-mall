package com.shopping.main.domain.user.dto;

import com.shopping.main.domain.user.entity.SiteUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

// 일반 로그인(UserDetails)과 소셜 로그인(OAuth2User)을 모두 처리하는 통합 클래스
@Getter
public class CustomUserDetails implements UserDetails, OAuth2User {

    private final SiteUser siteUser;
    private Map<String, Object> attributes; // 소셜 로그인 정보 담을 곳

    // 1. 일반 로그인용 생성자
    public CustomUserDetails(SiteUser siteUser) {
        this.siteUser = siteUser;
    }

    // 2. 소셜 로그인용 생성자
    public CustomUserDetails(SiteUser siteUser, Map<String, Object> attributes) {
        this.siteUser = siteUser;
        this.attributes = attributes;
    }

    // ★ 핵심: 닉네임을 꺼내는 메서드
    public String getNickname() {
        return siteUser.getNickname();
    }

    // --- 아래는 Spring Security 필수 구현 메서드들 ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(siteUser.getRole().name()));
    }

    @Override
    public String getPassword() {
        return siteUser.getPassword();
    }

    @Override
    public String getUsername() {
        return siteUser.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // OAuth2User 구현
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return siteUser.getEmail();
    }
}