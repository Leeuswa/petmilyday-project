package com.petmilyday.entity.member;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본키 (AUTO_INCREMENT)

    @Column(nullable = false, unique = true, length = 50)
    private String username; // 아이디

    @Column(length = 30)
    private String nickname; // 닉네임 (Null 허용)

    @Column(nullable = false, length = 100)
    private String password; // 비밀번호 (BCrypt 암호화)

    @Column(nullable = false, length = 30)
    private String name; // 이름

    @Column(nullable = false, unique = true, length = 100)
    private String email; // 이메일

    @Column(name = "social_type", length = 20)
    private String socialType; // 소셜 로그인 타입 (일반 가입은 NULL) [cite: 16]

    @Column(name = "social_id", length = 100)
    private String socialId; // 소셜 로그인 ID (일반 가입은 NULL) [cite: 16]

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role; // 권한 (USER/ADMIN) [cite: 16]

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status; // 계정 상태 (ACTIVE/BANNED/WITHDRAWN) [cite: 17]

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 가입일시 [cite: 17]

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 수정일시 [cite: 17]

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // 탈퇴일시 [cite: 17]

    @Column(name = "refresh_token", length = 500)
    private String refreshToken; // 리프레시 토큰 [cite: 18]

    @Column(name = "fcm_token")
    private String fcmToken; // FCM 토큰 [cite: 19]

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
