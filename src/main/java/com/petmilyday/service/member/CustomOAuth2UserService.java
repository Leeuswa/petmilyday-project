package com.petmilyday.service.member;

import com.petmilyday.entity.member.AccountStatus;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.member.Role;
import com.petmilyday.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> paramMap = oAuth2User.getAttributes();

        String email = null;
        String name = null;
        String nickname = null;

        if (provider.equals("kakao")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) paramMap.get("kakao_account");
            email = (String) kakaoAccount.get("email");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            nickname = (String) profile.get("nickname");
            name = nickname;
        } else if (provider.equals("google")) {
            email = (String) paramMap.get("email");
            name = (String) paramMap.get("name");
            nickname = name;
        }

        log.info("소셜 로그인 시도 이메일: " + email);

        final String finalProvider = provider;
        final String finalEmail = email;
        final String finalName = name;
        final String finalNickname = nickname;

        boolean isAlreadyRegistered = memberRepository.existsByEmail(finalEmail);

        Member member = memberRepository.findByEmail(finalEmail).orElseGet(() -> {
            String socialId = finalEmail.split("@")[0];
            String uniqueUsername = finalProvider + "_" + socialId;

            Member newMember = Member.builder()
                    .username(uniqueUsername)
                    .password(UUID.randomUUID().toString())
                    .name(finalName)
                    .nickname(finalNickname)
                    .email(finalEmail)
                    .socialType(finalProvider)
                    .socialId(socialId)
                    .role(Role.USER)
                    .status(AccountStatus.ACTIVE)
                    .build();

            Member saved = memberRepository.save(newMember);
            log.info("[INSERT SUCCESS] 신규 소셜 회원 DB 저장 완료 (회원 고유 식별번호: " + saved.getId() + ")");
            return saved;
        });

        if (isAlreadyRegistered) {
            if (member.getStatus() == AccountStatus.WITHDRAWN) {
                throw new UsernameNotFoundException("탈퇴한 회원입니다.");
            }

            log.info("[STATUS: EXISTING] 우리 서비스에 이미 가입된 기존 회원입니다.");
            log.info("   • 매핑된 기존 회원 닉네임: " + member.getNickname());
            log.info("   • 계정 상태              : " + member.getStatus());
        }

        log.info("=================================================================");
        log.info("[OAuth2 SUCCESS] 소셜 로그인 연동 최종 성공! 메인 페이지로 이동합니다.");
        log.info("=================================================================");

        Map<String, Object> modifiableAttributes = new HashMap<>(paramMap);
        modifiableAttributes.put("db_username", member.getUsername());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())),
                modifiableAttributes,
                userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()
        );
    }
}