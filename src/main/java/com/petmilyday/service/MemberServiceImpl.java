package com.petmilyday.service;

import com.petmilyday.dto.MemberDTO;
import com.petmilyday.entity.AccountStatus;
import com.petmilyday.entity.Member;
import com.petmilyday.entity.Role;
import com.petmilyday.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public MemberDTO.RegisterResponse register(MemberDTO.RegisterRequest request) {

        // 1. 아이디 및 이메일 중복 검증
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. 회원 엔티티 생성
        Member member = Member.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .name(request.getName())
                .email(request.getEmail())
                .nickname(request.getNickname() != null && !request.getNickname().isBlank() ? request.getNickname() : null)
                .role(Role.USER)
                .status(AccountStatus.ACTIVE)
                .build();

        // 4. DB에 저장
        Member savedMember = memberRepository.save(member);

        // 5. 응답 DTO 반환
        return MemberDTO.RegisterResponse.builder()
                .id(savedMember.getId())
                .username(savedMember.getUsername())
                .name(savedMember.getName())
                .email(savedMember.getEmail())
                .nickname(savedMember.getNickname())
                .createdAt(savedMember.getCreatedAt())
                .build();
    }
}