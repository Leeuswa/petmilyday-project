package com.petmilyday.controller;

import com.petmilyday.entity.member.Member;
import com.petmilyday.repository.member.MemberRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;

@ControllerAdvice // 모든 HTML 화면이 켜질 때마다 이 마법이 작동합니다.
public class GlobalControllerAdvice {

    private final MemberRepository memberRepository;

    // 생성자를 통해 의존성을 안전하게 주입받습니다.
    public GlobalControllerAdvice(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @ModelAttribute
    public void addAttributes(Model model, @AuthenticationPrincipal Object principal) {
        // 현재 로그인이 되어 있는 상태라면!
        if (principal != null) {
            String username;

            // 1. 일반 로그인 유저인 경우 (시큐리티가 ID를 들고 있음)
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            }
            // 2. 소셜 로그인(구글, 카카오) 유저인 경우
            else if (principal instanceof OAuth2User) {
                username = ((OAuth2User) principal).getName();
            } else {
                username = null;
            }

            if (username != null) {
                // 🔍 데이터베이스에서 로그인한 사람의 진짜 회원 정보(엔티티)를 찾아옵니다.
                memberRepository.findByUsername(username).ifPresentOrElse(
                        member -> {
                            // [명세서 규칙] 닉네임이 존재하면 닉네임 사용, 비어있으면 아이디(username) 사용
                            String nameToShow = (member.getNickname() != null && !member.getNickname().isBlank())
                                    ? member.getNickname()
                                    : member.getUsername();

                            // 화면 우체통(loggedInUser)에 닉네임을 쏙 넣어줍니다!
                            model.addAttribute("loggedInUser", nameToShow);
                        },
                        () -> {
                            // 🚨 방어 코드: 만약 아직 소셜 로그인 가입 처리가 DB에 안 되었을 때를 대비해
                            // 구글/카카오가 던져준 프로필에서 직접 닉네임을 임시로 추출해 띄워줍니다.
                            OAuth2User oAuth2User = (OAuth2User) principal;
                            String fallbackName = oAuth2User.getAttribute("name"); // 구글 기본값

                            if (oAuth2User.getAttribute("properties") != null) { // 카카오 기본값
                                Map<String, Object> properties = (Map<String, Object>) oAuth2User.getAttribute("properties");
                                if (properties.get("nickname") != null) {
                                    fallbackName = (String) properties.get("nickname");
                                }
                            }
                            model.addAttribute("loggedInUser", fallbackName != null ? fallbackName : username);
                        }
                );
            }
        }
    }
}