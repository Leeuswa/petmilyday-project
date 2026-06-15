package com.petmilyday.service.member;

import com.petmilyday.entity.member.AccountStatus;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.member.Role;
import com.petmilyday.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
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
        log.info("=================================================================");
        log.info(" [OAuth2] 소셜 로그인 인증 요청 수신 (loadUser 시작)");
        log.info("=================================================================");

        // 1. 어떤 소셜 플랫폼인지 파악 (Kakao 또는 Google)
        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        String clientName = clientRegistration.getClientName();
        log.info("▶ [PLATFORM] 로그인 시도한 소셜 서비스: " + clientName);

        // 2. 소셜 서버로부터 사용자 정보 원본 로드
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> paramMap = oAuth2User.getAttributes();

        // 개발자님 기존 코드: 원본 속성 데이터 싹 출력하기
        log.info("---  [RAW DATA] 소셜 서버가 전달해준 원본 Attributes 목록 ---");
        paramMap.forEach((k, v) -> {
            log.info("   ↳ " + k + " : " + v);
        });
        log.info("-----------------------------------------------------------------");

        String providerId;
        String email = "";
        String name = "";

        // 3. 플랫폼별 데이터 맞춤 정제
        if (clientName.equalsIgnoreCase("google")) {
            providerId = oAuth2User.getAttribute("sub");
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
        } else if (clientName.equalsIgnoreCase("kakao")) {
            providerId = String.valueOf(paramMap.get("id"));
            Map<String, Object> kakaoAccount = (Map<String, Object>) paramMap.get("kakao_account");
            Map<String, Object> properties = (Map<String, Object>) paramMap.get("properties");

            if (kakaoAccount != null) email = (String) kakaoAccount.get("email");
            if (properties != null) name = (String) properties.get("nickname");
        } else {
            providerId = "";
        }

        // 혹시 모를 이메일/이름 누락 대비 방어 코드
        if (email == null || email.isBlank()) email = providerId + "@" + clientName.toLowerCase() + ".com";
        if (name == null || name.isBlank()) name = "소셜유저_" + providerId.substring(0, Math.min(providerId.length(), 5));

        String generatedUsername = clientName.toLowerCase() + "_" + providerId;

        log.info("🔍 [PROCESSING] 데이터 정제 완료. DB 대조 작업을 시작합니다.");
        log.info("   • 매핑용 고유 ID 생성 결과 : " + generatedUsername);
        log.info("   • 대상 이메일 주소         : " + email);

        // 4.  DB에 이미 등록된 회원인지 검사 및 콘솔 로그 분기
        String finalEmail = email;
        String finalName = name;

        // 가입 유무 체크용 플래그
        boolean isAlreadyRegistered = memberRepository.findByEmail(finalEmail).isPresent();

        Member member = memberRepository.findByEmail(finalEmail).orElseGet(() -> {
            // [분기 1] DB에 이메일이 없는 경우 -> 신규 회원 가입 진행
            log.info("✨ [STATUS: NEW] DB에 없는 사용자입니다. 자동 회원가입 프로세스를 진행합니다.");
            Member newMember = Member.builder()
                    .username(generatedUsername)
                    .password(UUID.randomUUID().toString()) // 소셜 유저는 비밀번호 불필요하므로 무작위 토큰 저장
                    .name(finalName)
                    .email(finalEmail)
                    .nickname(finalName)
                    .socialType(clientName.toUpperCase())
                    .socialId(providerId)
                    .role(Role.USER)
                    .status(AccountStatus.ACTIVE)
                    .build();

            Member saved = memberRepository.save(newMember);
            log.info(" [INSERT SUCCESS] 신규 소셜 회원 DB 저장 완료 (회원 고유 식별번호: " + saved.getId() + ")");
            return saved;
        });

        // [분기 2] DB에 이미 이메일이 존재하는 경우 -> 기존 회원 로그인 처리
        if (isAlreadyRegistered) {

            // 탈퇴한 회원이 다시 로그인하는 것 방지
            if (member.getStatus() == AccountStatus.WITHDRAWN) {
                throw new UsernameNotFoundException("탈퇴한 회원입니다.");
            }

            log.info("🎉 [STATUS: EXISTING] 우리 서비스에 이미 가입된 기존 회원입니다.");
            log.info("   • 매핑된 기존 회원 닉네임: " + member.getNickname());
            log.info("   • 계정 상태              : " + member.getStatus());
        }

        log.info("=================================================================");
        log.info(" [OAuth2 SUCCESS] 소셜 로그인 연동 최종 성공! 메인 페이지로 이동합니다.");
        log.info("=================================================================");

        // 시큐리티 권한 및 주머니 속성 설정
        Map<String, Object> modifiableAttributes = new HashMap<>(paramMap);
        modifiableAttributes.put("db_username", member.getUsername());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().name())),
                modifiableAttributes,
                "db_username"
        );
    }
}