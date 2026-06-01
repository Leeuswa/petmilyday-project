package com.petmilyday.service.impl.member;

import com.petmilyday.config.jwt.JwtTokenProvider;
import com.petmilyday.dto.member.MemberDTO;
import com.petmilyday.entity.member.AccountStatus;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.member.Role;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public MemberDTO.RegisterResponse register(MemberDTO.RegisterRequest request) {

        // 아이디 및 이메일 중복 검증
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 회원 엔티티 생성
        Member member = Member.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .name(request.getName())
                .email(request.getEmail())
                .nickname(request.getNickname() != null && !request.getNickname().isBlank() ? request.getNickname() : null)
                .role(Role.USER)
                .status(AccountStatus.ACTIVE)
                .build();

        // DB에 저장
        Member savedMember = memberRepository.save(member);

        // 응답 DTO 반환
        return MemberDTO.RegisterResponse.builder()
                .id(savedMember.getId())
                .username(savedMember.getUsername())
                .name(savedMember.getName())
                .email(savedMember.getEmail())
                .nickname(savedMember.getNickname())
                .createdAt(savedMember.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void updateProfile(String username, MemberDTO.UpdateProfileRequest request) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // 이메일이 변경되었는데, 그 이메일이 이미 존재하는 경우 중복 체크
        if (!member.getEmail().equals(request.getEmail()) && memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 엔티티 값 변경 (JPA 더티 체킹으로 인해 DB에 자동 반영됨)
        member.updateProfile(request.getNickname(), request.getEmail());
    }

    @Override
    @Transactional
    public void updatePassword(String username, MemberDTO.UpdatePasswordRequest request) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // 소셜 로그인 회원은 비밀번호 변경 불가 처리 (선택 사항)
        if (member.getSocialType() != null) {
            throw new IllegalArgumentException("소셜 로그인 회원은 비밀번호를 변경할 수 없습니다.");
        }

        // 기존 비밀번호가 맞는지 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 암호화 후 변경
        member.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Override
    @Transactional
    public void withdraw(String username, String password) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // 일반 회원인 경우 비밀번호 확인 (소셜 회원은 패스워드가 임의값이므로 비밀번호 검증 생략)
        if (member.getSocialType() == null && !passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 계정 상태를 WITHDRAWN으로 변경
        member.withdraw();
    }

    @Override
    public MemberDTO.LoginResponse login(MemberDTO.LoginRequest request) {
        // 회원 조회
        Member member = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디이거나 비밀번호가 틀렸습니다."));

        // 탈퇴 회원 검증
        if (member.getStatus() == AccountStatus.WITHDRAWN) {
            throw new IllegalArgumentException("탈퇴한 회원입니다.");
        }

        // 비밀번호 대조
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("가입되지 않은 아이디이거나 비밀번호가 틀렸습니다.");
        }

        // JWT 토큰 생성 (권한 정보 포함)
        String token = jwtTokenProvider.createToken(member.getUsername(), member.getRole().name());

        // 토큰을 담아서 반환
        return MemberDTO.LoginResponse.builder()
                .token(token)
                .username(member.getUsername())
                .build();
    }
}