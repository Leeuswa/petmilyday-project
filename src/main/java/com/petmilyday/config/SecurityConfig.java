package com.petmilyday.config;

import com.petmilyday.config.jwt.JwtAuthenticationFilter;
import com.petmilyday.config.jwt.JwtTokenProvider;
import com.petmilyday.config.jwt.OAuth2SuccessHandler;
import com.petmilyday.service.member.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
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
                // 무상태 환경이므로 CSRF 공격 보호를 비활성화
                .csrf(csrf -> csrf.disable())

                .logout(logout -> logout
                        .logoutUrl("/member/logout")
                        .logoutSuccessUrl("/")
                        .deleteCookies("Authorization", "JSESSIONID")
                        .invalidateHttpSession(true)
                )

                // 폼 로그인 화면이나 기존 세션 기반 로그아웃 메커니즘을 사용하지 않으므로 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 스프링 시큐리티가 세션을 생성 및 사용하지 않도록 무상태
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendRedirect("/member/login");
                        })
                )

                // API별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth

                        // 정적 리소스 허용
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/img/**",
                                "/webjars/**",
                                "/favicon.ico",
                                "/",
                                "/error"
                        ).permitAll()

                        // 회원가입, 로그인, 토큰 재발급, 아이디 중복체크 허용
                        .requestMatchers(
                                "/member/register",
                                "/member/login",
                                "/member/reissue",
                                "/member/check-username"
                        ).permitAll()

                        // 공개 페이지 허용
                        .requestMatchers(
                                "/community/list",
                                "/community/meetup/**",
                                "/hospital/list",
                                "/shop/list",
                                "/ai/diagnosis"
                        ).permitAll()

                        // 쇼핑/구독 관련 공개 API
                        .requestMatchers(
                                "/shop/**",
                                "/api/subscription/**",
                                "/shop/subscription"
                        ).permitAll()

                        // SSE 알림 구독 경로 허용
                        .requestMatchers(
                                "/notifications/**",
                                "/api/notifications/**",
                                "/notification/**",
                                "/api/notification/**"
                        ).permitAll()

                        // 메인 관리자 페이지는 ADMIN 권한만 접근 가능
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 병원 관리자 페이지는 HOSPITAL_ADMIN 권한만 접근 가능
                        .requestMatchers(
                                "/hospitalAdmin/**",
                                "/hospital-admin/**"
                        ).hasRole("HOSPITAL_ADMIN")

                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // OAuth2 소셜 로그인도 JWT에 맞춰서 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )

                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                .requestMatchers(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/img/**",
                        "/favicon.ico",
                        "/error"
                );
    }
}