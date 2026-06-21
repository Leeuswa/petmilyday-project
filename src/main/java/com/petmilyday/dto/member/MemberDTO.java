package com.petmilyday.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

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

        @Size(max = 30, message = "닉네임은 30자 이내여야 합니다.")
        private String nickname;

        @NotBlank(message = "이름은 필수 입력 사항입니다.")
        @Size(max = 30, message = "이름은 30자 이내여야 합니다.")
        private String name;

        @NotBlank(message = "이메일은 필수 입력 사항입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$", message = "전화번호는 000-0000-0000 형식이어야 합니다.")
        private String phoneNumber;

        private String address;

        private String detailAddress;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        @Size(max = 30, message = "닉네임은 30자 이내여야 합니다.")
        private String nickname;

        @NotBlank(message = "이메일은 필수 입력 사항입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$", message = "전화번호는 000-0000-0000 형식이어야 합니다.")
        private String phoneNumber;

        private String address;

        private String detailAddress;

        @Size(max = 100, message = "한 줄 소개는 100자 이내여야 합니다.")
        private String bio;
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
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private String token;
        private String username;
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
        private String phoneNumber;
        private String address;
        private String detailAddress;
        private String bio;
    }

}