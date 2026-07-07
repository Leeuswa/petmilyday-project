package com.petmilyday.service.member;

import com.petmilyday.dto.member.MemberDTO;

public interface MemberService {
    // 회원가입 비즈니스 로직
    void register(MemberDTO.RegisterRequest request);

    // 중복 확인
    boolean checkUsername(String username);

    // 회원정보 수정
    void updateProfile(String username, MemberDTO.UpdateRequest request);

    // 비밀번호 변경
    void updatePassword(String username, MemberDTO.UpdatePasswordRequest request);

    // 회원 탈퇴
    void withdraw(String username);

    // 로그인 비즈니스 로직
    MemberDTO.LoginResponse login(MemberDTO.LoginRequest request);

    // 마이페이지 회원 정보 조회
    MemberDTO.MyPageResponse getMyPageInfo(String username);

}