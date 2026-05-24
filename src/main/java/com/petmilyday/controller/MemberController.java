package com.petmilyday.controller;

import com.petmilyday.dto.MemberDTO;
import com.petmilyday.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 1. 회원가입 화면 요청 (GET /member/register)
    @GetMapping("/register")
    public String registerForm(Model model) {
        // 타임리프 양식과 바인딩할 빈 DTO 객체를 모델에 담아 보냅니다.
        model.addAttribute("registerRequest", new MemberDTO.RegisterRequest());
        return "member/register"; // src/main/resources/templates/member/register.html 을 매핑
    }

    // 2. 회원가입 처리 요청 (POST /member/register)
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") MemberDTO.RegisterRequest request,
                           BindingResult bindingResult,
                           Model model) {

        // ⭐ 1. 백엔드(Spring) 규칙 검사에 걸린 경우 (조용히 새로고침되던 원인!)
        if (bindingResult.hasErrors()) {
            // 어떤 에러인지 찾아서 메시지 가져오기
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();

            // 개발자 확인용: 인텔리제이 아래쪽 콘솔창에 빨간 글씨로 이유 출력
            System.err.println("🚨 백엔드 회원가입 차단 사유: " + errorMessage);

            // 사용자 확인용: 프론트엔드 알림창으로 에러 메시지 띄워주기
            model.addAttribute("globalError", errorMessage);

            return "member/register"; // 다시 가입 창으로 돌려보냄
        }

        try {
            // ⭐ 2. 정상 통과 시 DB에 저장
            memberService.register(request);
            System.out.println("✅ 회원가입 성공! DB 저장 완료: " + request.getUsername());

        } catch (IllegalArgumentException e) {
            // 중복된 아이디나 이메일인 경우
            model.addAttribute("globalError", e.getMessage());
            return "member/register";
        }

        // 성공 시 로그인 페이지로 부드럽게 이동
        return "redirect:/member/login";
    }

    // 로그인 화면 요청 (GET /member/login)
    @GetMapping("/login")
    public String loginForm() {
        return "member/login";
    }
}