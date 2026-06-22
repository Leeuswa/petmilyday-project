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

    public GlobalControllerAdvice(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @ModelAttribute
    public void addAttributes(Model model, @AuthenticationPrincipal Object principal) {
        if (principal != null) {
            String username;

            // 일반 로그인 유저인 경우
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            }
            // 소셜 로그인(구글, 카카오) 유저인 경우
            else if (principal instanceof OAuth2User) {
                username = ((OAuth2User) principal).getName();
            } else {
                username = null;
            }

            if (username != null) {
                memberRepository.findByUsername(username).ifPresentOrElse(
                        member -> {
                            String nameToShow = (member.getNickname() != null && !member.getNickname().isBlank())
                                    ? member.getNickname()
                                    : member.getUsername();

                            model.addAttribute("loggedInUser", nameToShow);
                        },
                        () -> {
                            // 만약 아직 소셜 로그인 가입 처리가 DB에 안 되었을 때 대비
                            // 구글 & 카카오가 가지고 있는 정보 가져와서 임시로 씀
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