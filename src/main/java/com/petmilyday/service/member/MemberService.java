package com.petmilyday.service.member;

import com.petmilyday.dto.member.MemberDTO;

public interface MemberService {
    // 회원가입 비즈니스 로직
    MemberDTO.RegisterResponse register(MemberDTO.RegisterRequest request);

    void updateProfile(String username, MemberDTO.UpdateProfileRequest request);
    void updatePassword(String username, MemberDTO.UpdatePasswordRequest request);
    void withdraw(String username, String password);
    MemberDTO.LoginResponse login(MemberDTO.LoginRequest request);
    MemberDTO.MyPageResponse getMyPageInfo(String username);
}