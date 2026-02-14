package com.shopping.main.global.security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.shopping.main.domain.user.service.CustomOAuth2Service;
import com.shopping.main.domain.user.service.UserSecurityService;
import com.shopping.main.global.config.CustomAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserSecurityService userSecurityService;
    private final DataSource dataSource;
    private final CustomOAuth2Service customOAuth2Service;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/error").permitAll()

                .requestMatchers("/admin/**").hasAuthority("ADMIN")

                .requestMatchers("/", "/users/**", "/h2-console/**", "/products/**").permitAll()

                .anyRequest().authenticated())

                // H2 콘솔 사용을 위한 설정
                .csrf((csrf) -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers((headers) -> headers.frameOptions((frame) -> frame.sameOrigin()))

                // 1. 일반 로그인 (Form Login)
                .formLogin(form -> form
                        .loginPage("/users/login")
                        .defaultSuccessUrl("/"))
                // 2. 소셜 로그인 (OAuth2 Login)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/users/login")
                        .defaultSuccessUrl("/")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2Service)))

                // 3. 로그아웃
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/users/logout"))
                        .logoutSuccessUrl("/").invalidateHttpSession(true))

                // 4. 로그인 유지 (Remember-Me)
                .rememberMe(remember -> remember
                        .key("uniqueAndSecret")
                        .tokenRepository(tokenRepository())
                        .tokenValiditySeconds(86400 * 30)
                        .userDetailsService(userSecurityService));

        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);
        return repo;
    }
}
