package com.petmilyday.controller.admin;

import com.petmilyday.service.hospital.HospitalManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/hospital-managers")
public class AdminHospitalManagerController {

    private final HospitalManagerService hospitalManagerService;

    // 병원 관리자 신청 목록 (병원명/담당자명 검색)
    @GetMapping
    public String waitingList(@RequestParam(required = false) String keyword,
                              @RequestParam(defaultValue = "0") int page,
                              Model model) {

        model.addAttribute("managerPage", hospitalManagerService.waitingListPage(keyword, page));
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);

        return "admin/hospitalManager/hospitalManagerList";
    }

    // 병원 관리자 승인
    @PostMapping("/{id}/approve")
    public String approveManager(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {

        hospitalManagerService.approveManager(id);

        redirectAttributes.addFlashAttribute("message", "병원 관리자 신청을 승인했습니다.");

        return "redirect:/admin/hospital-managers";
    }

    // 병원 관리자 거절
    @PostMapping("/{id}/reject")
    public String rejectManager(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {

        hospitalManagerService.rejectManager(id);

        redirectAttributes.addFlashAttribute("message", "병원 관리자 신청을 거절했습니다.");

        return "redirect:/admin/hospital-managers";
    }
}