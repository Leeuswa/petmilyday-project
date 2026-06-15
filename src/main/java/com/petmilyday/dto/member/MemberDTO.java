package com.petmilyday.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

public class MemberDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequest {

        @NotBlank(message = "아이디는 필수 입력 사항입니다.")
        @Size(max = 20, message = "아이디는 20자 이내여야 합니다.")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문 및 숫자만 허용됩니다.")
        private String username;

        @NotBlank(message = "비밀번호는 필수 입력 사항입니다.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
                message = "비밀번호는 8~20자 이내 영문 대소문자, 숫자, 특수문자 조합이어야 합니다."
                )
        private String password;

        @NotBlank(message = "이름은 필수 입력 사항입니다.")
        @Size(max = 30, message = "이름은 30자 이내여야 합니다.")
        private String name;

        @NotBlank(message = "이메일은 필수 입력 사항입니다.")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
                message = "올바른 이메일 형식이 아닙니다. (예: example@petmily.com)")
        private String email;

        @Size(max = 30, message = "닉네임은 30자 이내여야 합니다.")
        private String nickname;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterResponse {
        private Long id;
        private String username;
        private String name;
        private String email;
        private String nickname;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateProfileRequest {
        @Size(max = 30, message = "닉네임은 30자 이내여야 합니다.")
        private String nickname;

        @NotBlank(message = "이메일은 필수 입력 사항입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatePasswordRequest {
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        private String currentPassword;

        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
                message = "비밀번호는 8~20자 이내 영문 대소문자, 숫자, 특수문자 조합이어야 합니다."
        )
        private String newPassword;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "아이디를 입력해주세요.")
        private String username;

        @NotBlank(message = "비밀번호를 입력해주세요.")
        private String password;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class LoginResponse {
        private String token;     // 발급된 JWT 문자열
        private String username;  // 로그인한 유저 아이디
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyPageResponse {
        private String username;
        private String name;
        private String nickname;
        private String email;
    }
}