package com.petmilyday.service.member;

import com.petmilyday.entity.member.AccountStatus;
import com.petmilyday.entity.member.Member;
import com.petmilyday.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. DB에서 사용자 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("가입되지 않은 아이디입니다."));

        // 탈퇴한 회원이 다시 로그인하는 것 방지
        if (member.getStatus() == AccountStatus.WITHDRAWN) {
            throw new UsernameNotFoundException("탈퇴한 회원입니다.");
        }

        // 2. 스프링 시큐리티가 이해할 수 있는 UserDetails 객체로 변환해서 전달
        return User.builder()
                .username(member.getUsername())
                .password(member.getPassword())
                .roles(member.getRole().name())
                .build();
    }
}