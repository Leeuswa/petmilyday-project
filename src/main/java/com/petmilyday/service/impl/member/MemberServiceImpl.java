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
    public void register(MemberDTO.RegisterRequest request) {

        // 아이디 및 이메일 중복 검증
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Member member = Member.builder()
                .username(request.getUsername())
                .nickname(request.getNickname())
                .password(encodedPassword)
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .detailAddress(request.getDetailAddress())
                .profileImageUrl(null)
                .bio("소개글이 없습니다.")
                .role(Role.USER)
                .status(AccountStatus.ACTIVE)
                .build();

        memberRepository.save(member);
    }

    // 아이디 중복 확인
    @Override
    @Transactional(readOnly = true)
    public boolean checkUsername(String username) {
        return memberRepository.existsByUsername(username);
    }

    @Override
    @Transactional
    public void updateProfile(String username, MemberDTO.UpdateRequest request) {
        // 회원 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        member.updateProfile(
                request.getName(),
                request.getNickname(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getAddress(),
                request.getDetailAddress(),
                request.getBio()
        );
    }

    @Override
    @Transactional
    public void updatePassword(String username, MemberDTO.UpdatePasswordRequest request) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), member.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 다르게 설정해야 합니다.");
        }

        member.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Override
    @Transactional
    public void withdraw(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 상태값 변경 방식 (Soft Delete)인 경우
        member.withdraw();

        // 만약 진짜 DB에서 데이터를 아예 지우는 물리 삭제(Hard Delete)를 원하시면 아래 주석을 해제하세요.
        // memberRepository.delete(member);
    }

    @Override
    @Transactional
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

    @Override
    @Transactional(readOnly = true)
    public MemberDTO.MyPageResponse getMyPageInfo(String username) {
        // 회원 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return MemberDTO.MyPageResponse.builder()
                .username(member.getUsername())
                .name(member.getName())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .address(member.getAddress())
                .detailAddress(member.getDetailAddress())
                .bio(member.getBio())
                .build();
    }
}