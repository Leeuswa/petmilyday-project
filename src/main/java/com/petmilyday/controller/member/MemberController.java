package com.petmilyday.controller.member;

import com.petmilyday.dto.member.MemberDTO;
import com.petmilyday.service.member.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 1. 회원가입 화면 요청 (GET /member/register)
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerRequest", new MemberDTO.RegisterRequest());
        return "member/register";
    }

    // 2. 회원가입 처리 요청 (POST /member/register)
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") MemberDTO.RegisterRequest request,
                           BindingResult bindingResult,
                           Model model) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            System.err.println("백엔드 회원가입 차단 사유: " + errorMessage);
            model.addAttribute("globalError", errorMessage);
            return "member/register";
        }

        try {
            memberService.register(request);
            System.out.println("회원가입 성공! DB 저장 완료: " + request.getUsername());
        } catch (IllegalArgumentException e) {
            model.addAttribute("globalError", e.getMessage());
            return "member/register";
        }

        return "redirect:/member/login";
    }

    // 3. 로그인 화면 요청 (GET /member/login)
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginRequest", new MemberDTO.LoginRequest());
        return "member/login";
    }

    // 4. 로그인 처리 (POST /member/login)
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginRequest") MemberDTO.LoginRequest request,
                        BindingResult bindingResult,
                        HttpServletResponse response,
                        Model model) {

        if (bindingResult.hasErrors()) {
            return "member/login";
        }

        try {
            MemberDTO.LoginResponse loginResponse = memberService.login(request);

            Cookie jwtCookie = new Cookie("JWT_TOKEN", loginResponse.getToken());
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(60 * 30);
            response.addCookie(jwtCookie);

            System.out.println("로그인 성공 및 토큰 발급 완료: " + loginResponse.getUsername());
            return "redirect:/";

        } catch (IllegalArgumentException e) {
            model.addAttribute("globalError", e.getMessage());
            return "member/login";
        }
    }

    // 5. 회원 정보 수정 처리 (POST /member/me/update-profile)
    @PostMapping("/me/update-profile")
    public String updateProfile(@Valid @ModelAttribute("updateRequest") MemberDTO.UpdateProfileRequest request,
                                BindingResult bindingResult,
                                Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("globalError", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "member/mypage";
        }

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            memberService.updateProfile(username, request);
        } catch (IllegalArgumentException e) {
            model.addAttribute("globalError", e.getMessage());
            return "member/mypage";
        }

        return "redirect:/member/mypage";
    }

    // 6. 회원 탈퇴 처리 (POST /member/me/withdraw)
    @PostMapping("/me/withdraw")
    public String withdraw(@RequestParam(required = false) String password,
                           HttpServletResponse response,
                           Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            memberService.withdraw(username, password);

            Cookie jwtCookie = new Cookie("JWT_TOKEN", null);
            jwtCookie.setMaxAge(0);
            jwtCookie.setPath("/");
            response.addCookie(jwtCookie);

            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("globalError", e.getMessage());
            return "member/mypage";
        }
    }

    // 7. 로그아웃 처리 (GET /member/logout)
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        // 1. JWT_TOKEN 이라는 이름의 쿠키를 텅 빈 값(null)으로 만듭니다.
        Cookie jwtCookie = new Cookie("JWT_TOKEN", null);

        // 2. 쿠키의 수명을 0초로 설정하여 브라우저가 즉시 삭제하도록 명령합니다.
        jwtCookie.setMaxAge(0);

        // 3. 쿠키가 적용되었던 전체 경로("/")를 명시해야 정확히 지워집니다.
        jwtCookie.setPath("/");

        // 4. 삭제 명령을 응답(Response)에 담아 브라우저로 보냅니다.
        response.addCookie(jwtCookie);

        // 5. 서버에 남아있을지도 모르는 시큐리티 인증 정보도 초기화합니다.
        SecurityContextHolder.clearContext();

        // 6. 메인 페이지로 이동시킵니다.
        return "redirect:/";
    }
}