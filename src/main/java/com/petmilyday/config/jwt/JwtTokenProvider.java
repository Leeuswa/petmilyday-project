package com.petmilyday.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

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
        // 문자열을 가반으로 안전한 HMAC-SHA 비밀키 생성
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    // 1. JWT 토큰 생성
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

    // 2. 토큰에서 인증 정보 조회 (SecurityContextHolder에 담을 객체 생성)
    public Authentication getAuthentication(String token) {
        String username = getUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 3. 토큰에서 회원 아이디(username) 추출
    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // 4. 토큰의 유효성 + 만료일자 확인 검증 검사
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return !claims.getPayload().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.error("❌ 잘못되거나 만료된 JWT 토큰입니다: {}", e.getMessage());
            return false;
        }
    }
}