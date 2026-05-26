package com.petmilyday.config;

import com.petmilyday.service.CustomOAuth2UserService;
import org.springframework.security.core.userdetails.UserDetailsService; // ⭐ 추가
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserDetailsService customUserDetailsService; // ⭐ 자동 로그인에서 유저 정보를 찾기 위해 추가

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 로컬 테스트용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/", "/member/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/member/login")
                        .loginProcessingUrl("/member/login")
                        .defaultSuccessUrl("/")
                        .failureUrl("/member/login?error=true")
                        .permitAll()
                )
                // 자동 로그인 (Remember-Me) 설정
                .rememberMe(remember -> remember
                        .rememberMeParameter("remember-me") // HTML 체크박스의 name
                        .key("petmilyDaySecretKey")         // 쿠키를 암호화할 고유 키 (아무 문자열이나 가능)
                        .tokenValiditySeconds(86400 * 30)   // 쿠키 유지 시간 (초 단위) = 30일
                        .userDetailsService(customUserDetailsService)
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/member/login")
                        .defaultSuccessUrl("/")
                        .failureUrl("/member/login?error=true")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                )
                .logout(logout -> logout
                        .logoutUrl("/member/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me") // ⭐ 로그아웃 시 자동 로그인 쿠키도 삭제
                        .permitAll()
                );

        return http.build();
    }
}