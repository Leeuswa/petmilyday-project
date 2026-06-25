package com.petmilyday.dto.admin;

import com.petmilyday.entity.member.AccountStatus;
import com.petmilyday.entity.member.Role;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSearchDTO {

    private String keyword; // 아이디/닉네임/이메일 검색
    private Role role;
    private AccountStatus status;
}
