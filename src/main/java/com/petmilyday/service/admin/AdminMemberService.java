package com.petmilyday.service.admin;

import com.petmilyday.dto.admin.AdminMemberDTO;
import com.petmilyday.entity.member.Role;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AdminMemberService {

    // 관리자 회원 목록 페이징 조회
    Page<AdminMemberDTO> memberList(int page);

    // 회원 정지
    void banMember(Long memberId, String currentUsername);

    // 회원 정지 해제
    void activateMember(Long memberId, String currentUsername);

    // 회원 권한 변경
    void changeRole(Long memberId, Role role, String currentUsername);
}