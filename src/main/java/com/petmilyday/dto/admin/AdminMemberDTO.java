package com.petmilyday.dto.admin;
import com.petmilyday.entity.member.AccountStatus;
import com.petmilyday.entity.member.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminMemberDTO {

    private Long id;

    private String username;

    private String nickname;

    private String name;

    private String email;

    private Role role;

    private AccountStatus status;

    private LocalDateTime createdAt;

}