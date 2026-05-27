package com.petmilyday.service.member;

import com.petmilyday.dto.member.MemberDTO;

public interface MemberService {
    // 회원가입 비즈니스 로직 [cite: 7]
    MemberDTO.RegisterResponse register(MemberDTO.RegisterRequest request);
}