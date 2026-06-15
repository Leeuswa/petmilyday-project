package com.petmilyday.controller.member;

import com.petmilyday.dto.member.MemberDTO;
import com.petmilyday.dto.member.PetProFileDTO;
import com.petmilyday.service.member.MemberService;
import com.petmilyday.service.member.PetProfileService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PetProfileService petProfileService;

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
        Cookie jwtCookie = new Cookie("JWT_TOKEN", null);
        jwtCookie.setMaxAge(0); // 쿠키 0초로 설정
        jwtCookie.setPath("/");
        response.addCookie(jwtCookie);
        SecurityContextHolder.clearContext();
        return "redirect:/";
    }

    @GetMapping("/mypage")
    public String myPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/member/login";
        }

        String username = auth.getName();
        MemberDTO.MyPageResponse myPageResponse = memberService.getMyPageInfo(username);

        model.addAttribute("member", myPageResponse);
        return "member/mypage";
    }

    @GetMapping("/modify-profile")
    public String modifyProfileForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        MemberDTO.MyPageResponse myInfo = memberService.getMyPageInfo(username);

        MemberDTO.UpdateProfileRequest updateRequest = new MemberDTO.UpdateProfileRequest();
        updateRequest.setNickname(myInfo.getNickname());
        updateRequest.setEmail(myInfo.getEmail());

        model.addAttribute("updateRequest", updateRequest);
        return "member/modify-profile";
    }

    @PostMapping("/modify-profile")
    public String modifyProfile(@Valid @ModelAttribute("updateRequest") MemberDTO.UpdateProfileRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        if (bindingResult.hasErrors()) {
            return "member/modify-profile";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        memberService.updateProfile(username, request);

        redirectAttributes.addFlashAttribute("successMsg", "회원정보가 성공적으로 수정되었습니다.");
        return "redirect:/member/mypage";
    }

    @GetMapping("/modify-password")
    public String modifyPasswordForm(Model model) {
        model.addAttribute("passwordRequest", new MemberDTO.UpdatePasswordRequest());
        return "member/modify-password";
    }

    // [데이터 처리] 비밀번호 변경 완료
    @PostMapping("/modify-password")
    public String modifyPassword(@Valid @ModelAttribute("passwordRequest") MemberDTO.UpdatePasswordRequest request,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "member/modify-password";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {
            memberService.updatePassword(username, request);

            redirectAttributes.addFlashAttribute("successMsg", "비밀번호가 성공적으로 변경되었습니다.");
            return "redirect:/member/mypage";

        } catch (IllegalArgumentException e) { // 기존 비번 틀릴 경우 통제
            model.addAttribute("globalError", e.getMessage());
            return "member/modify-password";
        }
    }

    @GetMapping("/pet-profile")
    public String petProfilePage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        List<PetProFileDTO> petList = petProfileService.petList(username);
        model.addAttribute("petList", petList);
        model.addAttribute("newPet", new PetProFileDTO());

        return "member/pet-profile";
    }

    @PostMapping("/pet-profile/register")
    public String registerPet(@Valid @ModelAttribute("newPet") PetProFileDTO dto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        if (bindingResult.hasErrors()) {
            List<PetProFileDTO> petList = petProfileService.petList(username);
            model.addAttribute("petList", petList);
            return "member/pet-profile";
        }

        petProfileService.registerPet(username, dto);
        redirectAttributes.addFlashAttribute("successMsg", "반려동물이 성공적으로 등록되었습니다.");
        return "redirect:/member/pet-profile";
    }

    @PostMapping("/pet-profile/delete/{id}")
    public String deletePet(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {
            petProfileService.deletePet(id, username);
            redirectAttributes.addFlashAttribute("successMsg", "프로필이 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/member/pet-profile";
    }
}