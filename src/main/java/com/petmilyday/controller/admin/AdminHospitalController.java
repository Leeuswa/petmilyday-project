package com.petmilyday.controller.admin;

import com.petmilyday.dto.admin.AdminHospitalDTO;
import com.petmilyday.service.admin.AdminHospitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/hospitals")
public class AdminHospitalController {

    private final AdminHospitalService adminHospitalService;

    // 병원 목록 조회
    @GetMapping("/")
    public String hospitalList(@RequestParam(defaultValue = "0") int page,
                               Model model) {

        model.addAttribute("hospitalPage", adminHospitalService.findAllPage(page));
        model.addAttribute("currentPage", page);

        return "admin/hospitals/hospitalList";
    }

    // 병원 등록 화면
    @GetMapping("/register")
    public String registerForm(Model model) {

        model.addAttribute("hospitalDTO", new AdminHospitalDTO());

        return "admin/hospitals/hospitalForm";
    }

    // 병원 등록 처리
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("hospitalDTO") AdminHospitalDTO dto,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "admin/hospitals/hospitalForm";
        }

        adminHospitalService.register(dto);

        redirectAttributes.addFlashAttribute("message", "병원이 등록되었습니다.");

        return "redirect:/admin/hospitals/";
    }

    // 병원 상세 조회
    @GetMapping("/{hospitalId}")
    public String hospitalDetail(@PathVariable Long hospitalId,
                                 Model model) {

        model.addAttribute("hospitalDTO", adminHospitalService.findById(hospitalId));

        return "admin/hospitals/hospitalDetail";
    }

    // 병원 수정 화면
    @GetMapping("/{hospitalId}/modify")
    public String modifyForm(@PathVariable Long hospitalId,
                             Model model) {

        model.addAttribute("hospitalDTO", adminHospitalService.findById(hospitalId));

        return "admin/hospitals/hospitalModify";
    }

    // 병원 수정 처리
    @PostMapping("/{hospitalId}/modify")
    public String modify(@PathVariable Long hospitalId,
                         @Valid @ModelAttribute("hospitalDTO") AdminHospitalDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "admin/hospitals/hospitalModify";
        }

        adminHospitalService.modify(hospitalId, dto);

        redirectAttributes.addFlashAttribute("message", "병원 정보가 수정되었습니다.");

        return "redirect:/admin/hospitals/" + hospitalId;
    }

    // 병원 삭제
    @PostMapping("/{hospitalId}/remove")
    public String remove(@PathVariable Long hospitalId,
                         RedirectAttributes redirectAttributes) {

        adminHospitalService.remove(hospitalId);

        redirectAttributes.addFlashAttribute("message", "병원이 삭제되었습니다.");

        return "redirect:/admin/hospitals/";
    }
}