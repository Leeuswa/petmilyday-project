package com.petmilyday.repository.member;

import com.petmilyday.dto.admin.MemberSearchDTO;
import com.petmilyday.entity.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {

    // 메인 어드민 - 회원 검색/필터 + 페이징
    Page<Member> searchMembersPage(MemberSearchDTO searchDTO, Pageable pageable);
}
