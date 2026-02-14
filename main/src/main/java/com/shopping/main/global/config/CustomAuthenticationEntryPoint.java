package com.shopping.main.global.config;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authenticationException) throws IOException, ServletException {
        // 1. AJAX 요청인지 확인 (헤더 검사)
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            // 2. AJAX라면 401 에러 코드 전송
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        } else {
            // 3. 일반 요청(주소창 입력 등)이라면 로그인 페이지로 리다이렉트
            response.sendRedirect("/users/login");
        }
    }
}
