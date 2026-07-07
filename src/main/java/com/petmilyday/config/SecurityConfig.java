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

                // JWT 쿠키 기반 로그아웃 처리
                .logout(logout -> logout
                        .logoutUrl("/member/logout")
                        .logoutSuccessUrl("/")
                        .deleteCookies("Authorization", "JWT_TOKEN", "JSESSIONID")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )

                // 폼 로그인, HTTP Basic 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // JWT 기반이므로 세션 사용 안 함
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 인증이 필요한 페이지 접근 시 로그인 페이지로 이동
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendRedirect("/member/login");
                        })
                )

                // 접근 권한 설정
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

                        // 회원 관련 공개 경로
                        .requestMatchers(
                                "/member/register",
                                "/member/login",
                                "/member/reissue",
                                "/member/check-username"
                        ).permitAll()

                        // 공개 페이지
                        .requestMatchers(
                                "/community/list",
                                "/community/meetup/**",
                                "/hospital/list",
                                "/shop/list",
                                "/ai/diagnosis"
                        ).permitAll()

                        // 쇼핑/구독 관련 공개 경로
                        .requestMatchers(
                                "/shop/**",
                                "/api/subscription/**",
                                "/shop/subscription"
                        ).permitAll()

                        // 알림/SSE 경로 허용
                        .requestMatchers(
                                "/notifications/**",
                                "/api/notifications/**",
                                "/notification/**",
                                "/api/notification/**"
                        ).permitAll()

                        // 메인 관리자
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 병원 관리자
                        .requestMatchers(
                                "/hospitalAdmin/**",
                                "/hospital-admin/**"
                        ).hasRole("HOSPITAL_ADMIN")

                        // 나머지는 로그인 필요
                        .anyRequest().authenticated()
                )

                // OAuth2 소셜 로그인
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )

                // JWT 필터 등록
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