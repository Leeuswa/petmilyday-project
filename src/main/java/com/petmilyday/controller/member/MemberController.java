package com.petmilyday.controller.member;

import com.petmilyday.dto.member.MemberDTO;
import com.petmilyday.dto.member.PetProFileDTO;
import com.petmilyday.service.member.MemberService;
import com.petmilyday.service.member.PetProfileService;
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

    // 회원가입 요청
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerRequest", new MemberDTO.RegisterRequest());
        return "member/register";
    }

    // 회원가입 처리
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") MemberDTO.RegisterRequest dto,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        // 유효성 검사
        if (bindingResult.hasErrors()) {
            return "member/register";
        }

        try {
            memberService.register(dto);
            redirectAttributes.addFlashAttribute("successMsg", "회원가입이 성공적으로 완료되었습니다.");
            return "redirect:/member/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("globalError", e.getMessage());
            return "redirect:/member/register";
        }
    }

    // 로그인 요청
    @GetMapping("/login")
    public String loginForm() {
        return "member/login";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(@ModelAttribute MemberDTO.LoginRequest loginRequest,
                        jakarta.servlet.http.HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        try {
            MemberDTO.LoginResponse loginResponse = memberService.login(loginRequest);

            jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("Authorization", loginResponse.getToken());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24);
            response.addCookie(cookie);

            return "redirect:/";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("globalError", e.getMessage());
            return "redirect:/member/login";
        }
    }

    // 아이디 중복 확인
    @GetMapping("/check-username")
    @ResponseBody
    public org.springframework.http.ResponseEntity<Boolean> checkUsername(@RequestParam("username") String username) {
        boolean isDuplicate = memberService.checkUsername(username);
        return org.springframework.http.ResponseEntity.ok(isDuplicate);
    }

    // 로그아웃 요청
    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpServletResponse response,
                         RedirectAttributes redirectAttributes) {

        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("Authorization", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        redirectAttributes.addFlashAttribute("successMsg", "안전하게 로그아웃 되었습니다.");
        return "redirect:/";
    }

    // 로그아웃 처리
    @PostMapping("/logout")
    public String logoutPost(jakarta.servlet.http.HttpServletResponse response,
                             RedirectAttributes redirectAttributes) {
        return logout(response, redirectAttributes);
    }

    // 마이페이지
    @GetMapping("/mypage")
    public String myPage(Authentication authentication, Model model) {
        String username = authentication.getName();
        MemberDTO.MyPageResponse myPageInfo = memberService.getMyPageInfo(username);
        model.addAttribute("member", myPageInfo);
        return "member/mypage";
    }

    // 회원정보 수정 화면
    @GetMapping("/modify-profile")
    public String modifyProfileForm(Authentication authentication, Model model) {
        String username = authentication.getName();
        MemberDTO.MyPageResponse info = memberService.getMyPageInfo(username);

        MemberDTO.UpdateRequest updateRequest = MemberDTO.UpdateRequest.builder()
                .nickname(info.getNickname())
                .email(info.getEmail())
                .phoneNumber(info.getPhoneNumber())
                .address(info.getAddress())
                .detailAddress(info.getDetailAddress())
                .bio(info.getBio())
                .build();

        model.addAttribute("updateRequest", updateRequest);
        return "member/modify-profile";
    }

    // 회원정보 수정 처리
    @PostMapping("/modify-profile")
    public String modifyProfile(@Valid @ModelAttribute("updateRequest") MemberDTO.UpdateRequest dto,
                                BindingResult bindingResult,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "member/modify-profile";
        }

        String username = authentication.getName();
        try {
            memberService.updateProfile(username, dto);
            redirectAttributes.addFlashAttribute("message", "회원 정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("globalError", e.getMessage());
            return "redirect:/member/modify-profile";
        }

        return "redirect:/member/mypage";
    }

    // 비밀번호 변경 요청
    @GetMapping("/modify-password")
    public String modifyPasswordForm(Model model) {
        model.addAttribute("passwordRequest", new MemberDTO.UpdatePasswordRequest());
        return "member/modify-password";
    }

    // 비밀번호 변경 처리
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
            redirectAttributes.addFlashAttribute("successMsg", "비밀번호가 성공적으로 변경되었습니다. 다시 로그인해 주세요.");
            return "redirect:/member/logout"; // 변경 후 안전하게 자동 로그아웃 처리
        } catch (IllegalArgumentException e) {
            model.addAttribute("globalError", e.getMessage());
            return "member/modify-password";
        }
    }

    @GetMapping("/pet-profile")
    public String petProfileForm(Model model) {
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