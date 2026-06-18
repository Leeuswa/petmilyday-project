package com.petmilyday.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Log4j2
@Component
public class JwtTokenProvider {

    // application.properties에 jwt.secret=지정할긴문자열(최소32자이상) 설정 필요
    @Value("${jwt.secret:defaultSecretKeyKeyKeyKeyKeyKeyKeyKeyKeyKeyKeyKeyKeyKeyKey}")
    private String secretKeyString;
    private SecretKey secretKey;

    // 토큰 유효시간 설정 (예: 30분)
    private final long tokenValidityInMilliseconds = 1000L * 60 * 30;

    private final UserDetailsService userDetailsService;

    public JwtTokenProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostConstruct
    protected void init() {
        // 문자열 가반 HMAC-SHA 비밀키 생성
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    // JWT 토큰 생성
    public String createToken(String username, String role) {
        Claims claims = Jwts.claims().subject(username).build();
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);

        return Jwts.builder()
                .claims(claims)
                .claim("role", role) // 커스텀 클레임으로 권한 정보 추가
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    // 토큰에서 인증 정보 조회
    public Authentication getAuthentication(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

            User principal = new User(username, "", authorities);

            return new UsernamePasswordAuthenticationToken(principal, token, authorities);

        } catch (ExpiredJwtException e) {
            // 토큰 유효기간이 지났을 때
            log.error("만료된 JWT 토큰입니다: {}", e.getMessage());

        } catch (JwtException | IllegalArgumentException e) {
            // 토큰이 조작되었거나 깨졌을 때
            log.error("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
        }
        return null;
    }

    // 토큰에서 회원 아이디 추출
    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // 토큰의 유효성 + 만료일자 확인 검증 검사
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return !claims.getPayload().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.error("잘못되거나 만료된 JWT 토큰입니다: {}", e.getMessage());
            return false;
        }
    }
}