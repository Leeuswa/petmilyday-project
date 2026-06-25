package com.petmilyday.controller.admin;

import com.petmilyday.dto.admin.MemberSearchDTO;
import com.petmilyday.entity.member.Role;
import com.petmilyday.service.admin.AdminMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminMemberService adminMemberService;


    // 관리자 회원 목록 검색/필터 + 페이징 조회
    @GetMapping("/admin/members")
    public String memberList(MemberSearchDTO searchDTO,
                             @RequestParam(defaultValue = "0") int page,
                             Model model) {

        model.addAttribute("memberPage", adminMemberService.memberList(searchDTO, page));
        model.addAttribute("currentPage", page);
        model.addAttribute("searchDTO", searchDTO);

        return "admin/member/memberList";
    }

    // 회원 정지
    @PostMapping("/admin/members/ban")
    public String banMember(@RequestParam Long memberId,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {

        try {
            adminMemberService.banMember(memberId, authentication.getName());
            redirectAttributes.addFlashAttribute("message", "회원이 정지되었습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/members";
    }

    // 회원 정지 해제
    @PostMapping("/admin/members/activate")
    public String activateMember(@RequestParam Long memberId,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {

        try {
            adminMemberService.activateMember(memberId, authentication.getName());
            redirectAttributes.addFlashAttribute("message", "회원 정지가 해제되었습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/members";
    }

    // 회원 권한 변경
    @PostMapping("/admin/members/role")
    public String changeRole(@RequestParam Long memberId,
                             @RequestParam Role role,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {

        try {
            adminMemberService.changeRole(memberId, role, authentication.getName());
            redirectAttributes.addFlashAttribute("message", "회원 권한이 변경되었습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/members";
    }
}