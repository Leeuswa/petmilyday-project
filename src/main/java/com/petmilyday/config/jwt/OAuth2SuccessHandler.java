package com.petmilyday.config.jwt;

import com.petmilyday.entity.member.Member;
import com.petmilyday.repository.member.MemberRepository;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String username = (String) oAuth2User.getAttributes().get("db_username");

        if (username == null) {
            response.sendRedirect("/member/login?error=" +
                    URLEncoder.encode("소셜 가입 중 오류가 발생했습니다.", StandardCharsets.UTF_8));
            return;
        }

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("소셜 로그인 회원을 찾을 수 없습니다."));

        String role = member.getRole().name();

        String token = jwtTokenProvider.createToken(member.getUsername(), role);

        Cookie jwtCookie = new Cookie("Authorization", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 30);
        jwtCookie.setAttribute("SameSite", "Lax");

        response.addCookie(jwtCookie);

        response.sendRedirect("/");
    }
}