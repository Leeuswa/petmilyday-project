package com.petmilyday.config;

import com.petmilyday.config.jwt.JwtAuthenticationFilter;
import com.petmilyday.config.jwt.JwtTokenProvider;
import com.petmilyday.config.jwt.OAuth2SuccessHandler;
import com.petmilyday.service.member.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 무상태(Stateless) 환경이므로 CSRF 공격 보호를 비활성화 (토큰이 쿠키 세션을 대체하므로 안전)
                .csrf(csrf -> csrf.disable())

                // 2. 폼 로그인 화면이나 기존 세션 기반 로그아웃 메커니즘을 사용하지 않으므로 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 3. 스프링 시큐리티가 세션을 완전히 생성하지도 않고 사용하지도 않도록 무상태 확정
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendRedirect("/member/login");
                        })
                )

                // 4. API별 접근 권한(인가) 설정
                .authorizeHttpRequests(auth -> auth
                        // 회원가입, 로그인, 토큰 재발급 등 인증이 필요 없는 경로 허용
                        .requestMatchers("/shop/**", "/api/subscription/**","shop/subscription").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/", "/error").permitAll()
                        .requestMatchers("/member/register", "/member/login", "/member/reissue").permitAll()
                        .requestMatchers("/community/list", "/hospital/list", "/shop/list", "/ai/diagnosis").permitAll()
                        // 나머지 모든 회원 수정, 탈퇴, 반려동물 프로필 등 API는 JWT 인증 필수
                        .anyRequest().authenticated()
                )

                // 5. OAuth2 소셜 로그인도 JWT 무상태 흐름에 맞추기 위해 설정 유지 (성공 핸들러 추가 필요)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler) // 성공 핸들러 명시적 등록
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}