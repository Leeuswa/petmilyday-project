package com.petmilyday.service.impl.admin;

import com.petmilyday.dto.admin.AdminMemberDTO;
import com.petmilyday.dto.admin.MemberSearchDTO;
import com.petmilyday.entity.member.AccountStatus;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.member.Role;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.service.admin.AdminMemberService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMemberServiceImpl implements AdminMemberService {

    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;

    // 메인 관리자가 회원 목록을 검색/필터 + 최신 가입순으로 페이징 조회
    @Override
    public Page<AdminMemberDTO> memberList(MemberSearchDTO searchDTO, int page) {

        Pageable pageable = PageRequest.of(page, 10);

        return memberRepository.searchMembersPage(searchDTO, pageable)
                .map(member -> modelMapper.map(member, AdminMemberDTO.class));
    }

    // 회원 정지 처리
    @Override
    @Transactional
    public void banMember(Long memberId, String currentUsername) {

        // 회원 ID로 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        // 관리자가 자기 자신을 정지하지 못하게 방지
        if (member.getUsername().equals(currentUsername)) {
            throw new RuntimeException("본인 계정은 변경할 수 없습니다.");
        }

        // 탈퇴한 회원은 정지 처리 불가
        if (member.getStatus() == AccountStatus.WITHDRAWN) {
            throw new RuntimeException("탈퇴한 회원은 변경할 수 없습니다.");
        }

        // 회원 상태를 BANNED로 변경
        member.ban();
    }

    // 회원 정지 해제 처리
    @Override
    @Transactional
    public void activateMember(Long memberId, String currentUsername) {

        // 회원 ID로 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        // 관리자가 자기 자신을 정지 해제하지 못하게 방지
        if (member.getUsername().equals(currentUsername)) {
            throw new RuntimeException("본인 계정은 변경할 수 없습니다.");
        }

        // 탈퇴한 회원은 정지 해제 처리 불가
        if (member.getStatus() == AccountStatus.WITHDRAWN) {
            throw new RuntimeException("탈퇴한 회원은 변경할 수 없습니다.");
        }

        // 회원 상태를 ACTIVE로 변경
        member.activate();
    }

    // 회원 권한 변경 처리
    @Override
    @Transactional
    public void changeRole(Long memberId, Role role, String currentUsername) {

        // 회원 ID로 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        // 관리자가 자기 자신의 권한을 변경하지 못하게 방지
        if (member.getUsername().equals(currentUsername)) {
            throw new RuntimeException("본인 계정은 변경할 수 없습니다.");
        }

        // 탈퇴한 회원은 권한 변경 불가
        if (member.getStatus() == AccountStatus.WITHDRAWN) {
            throw new RuntimeException("탈퇴한 회원은 변경할 수 없습니다.");
        }

        // USER/ADMIN을 HOSPITAL_ADMIN으로 직접 바꾸는 것은 금지 (병원관리자 신청-승인 절차로만 부여)
        if (role == Role.HOSPITAL_ADMIN && member.getRole() != Role.HOSPITAL_ADMIN) {
            throw new RuntimeException("병원 관리자 권한은 '병원 관리자 신청' 승인 절차로만 부여할 수 있습니다.");
        }

        // 회원 권한 변경
        member.changeRole(role);
    }
}