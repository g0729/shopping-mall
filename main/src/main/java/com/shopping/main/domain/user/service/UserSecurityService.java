package com.shopping.main.domain.user.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.shopping.main.domain.user.dto.CustomUserDetails;
import com.shopping.main.domain.user.entity.SiteUser;
import com.shopping.main.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSecurityService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<SiteUser> _siteUser = userRepository.findByEmail(email);

        if (_siteUser.isEmpty()) {
            throw new UsernameNotFoundException("해당 이메일이 존재하지 않습니다." + email);
        }

        SiteUser siteUser = _siteUser.get();

        return new CustomUserDetails(siteUser);
    }
}
