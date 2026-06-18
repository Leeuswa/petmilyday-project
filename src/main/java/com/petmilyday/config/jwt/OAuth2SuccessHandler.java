package com.petmilyday.config.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 인증된 소셜 유저 객체 꺼내기
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // CustomOAuth2UserService에서 주머니에 넣어두었던 db_username 값 가져오기
        String username = (String) oAuth2User.getAttributes().get("db_username");

        // 서비스 권한 체계에 맞춰 권한 정보 지정 (기본 USER)
        String role = "USER";

        // 무상태 전용 JWT 토큰 문자열 발행
        String token = jwtTokenProvider.createToken(username, role);

        // 발행된 토큰을 타임리프 브라우저가 읽을 수 있도록 쿠키에 저장
        Cookie jwtCookie = new Cookie("Authorization", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 30); // 쿠키 수명 30분 설정

        response.addCookie(jwtCookie);

        // 인증과 쿠키 설정이 끝났으므로 메인 페이지로 이동
        response.sendRedirect("/");
    }
}