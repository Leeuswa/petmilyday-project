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
    private Long id; // 번호 (AUTO_INCREMENT)

    @Column(nullable = false, unique = true, length = 50)
    private String username; // 아이디

    @Column(length = 30)
    private String nickname; // 닉네임

    @Column(nullable = false, length = 100)
    private String password; // 비밀번호 (BCrypt 암호화)

    @Column(nullable = false, length = 30)
    private String name; // 이름

    @Column(nullable = false, unique = true, length = 100)
    private String email; // 이메일

    @Column(name = "phone_number", length = 20)
    private String phoneNumber; // 전화번호

    @Column(nullable = false, length = 255)
    private String address; // 주소

    @Column(name = "detail_address", length = 100)
    private String detailAddress; // 상세 주소

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl; // 프로필 이미지 URL

    @Column(length = 100)
    private String bio; // 한 줄 소개

    @Column(name = "social_type", length = 20)
    private String socialType; // 소셜 로그인 타입

    @Column(name = "social_id", length = 100)
    private String socialId; // 소셜 로그인 ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role; // 권한 (USER/ADMIN)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status; // 계정 상태 (ACTIVE/BANNED/WITHDRAWN)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 가입일시

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 수정일시

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // 탈퇴일시

    @Column(name = "refresh_token", length = 500)
    private String refreshToken; // 리프레시 토큰

    @Column(name = "fcm_token")
    private String fcmToken; // FCM 토큰

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 회원 정보 수정
    public void updateProfile(String nickname, String email, String phoneNumber, String address, String detailAddress, String bio) {
        this.nickname = nickname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.detailAddress = detailAddress;
        this.bio = bio;
    }

    // 비밀번호 변경
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    // 회원 탈퇴
    public void withdraw() {
        this.status = AccountStatus.WITHDRAWN;
    }

    public String getDisplayName() {
        if (this.nickname != null && !this.nickname.trim().isEmpty()) {
            return this.nickname;
        }
        return this.username;
    }

    //권한 변경
    public void changeRole(Role role){
        this.role = role;
    }


    // 회원 정지
    public void ban() {
        this.status = AccountStatus.BANNED;
        this.updatedAt = LocalDateTime.now();
    }

    // 회원 정지 해제
    public void activate() {
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

}
