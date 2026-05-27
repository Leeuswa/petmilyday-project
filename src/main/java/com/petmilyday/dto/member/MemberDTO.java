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
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 100, message = "이메일은 100자 이내여야 합니다.")
        private String email;

        @Size(max = 30, message = "닉네임은 30자 이내여야 합니다.")
        private String nickname; // 전달되지 않거나 비어있으면 서비스단에서 처리 (NULL 허용) [cite: 1, 7]
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
}