package com.petmilyday.controller.hospital;

import com.petmilyday.dto.medical.MedicalRecordResponseDTO;
import com.petmilyday.service.medical.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hospitalAdmin/medicalRecords")
public class HospitalAdminMedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    // 병원 관리자 진료기록 작성 페이지
    @GetMapping("/register/{reservationId}")
    public String registerForm(@PathVariable Long reservationId,
                               Authentication authentication,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        try {
            MedicalRecordResponseDTO dto =
                    medicalRecordService.prepareRegisterForm(reservationId, authentication.getName());

            model.addAttribute("medicalRecordDTO", dto);
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("mode", "register");

            return "hospitalAdmin/medicalRecords/medicalRecordForm";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/hospitalAdmin/reservations";
        }
    }

    // 병원 관리자 진료기록 저장
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("medicalRecordDTO") MedicalRecordResponseDTO dto,
                           BindingResult bindingResult,
                           Authentication authentication,
                           Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("mode", "register");
            return "hospitalAdmin/medicalRecords/medicalRecordForm";
        }

        try {
            medicalRecordService.register(dto, authentication.getName());
        } catch (RuntimeException e) {
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("mode", "register");
            model.addAttribute("error", e.getMessage());
            return "hospitalAdmin/medicalRecords/medicalRecordForm";
        }

        return "redirect:/hospitalAdmin/reservations";
    }

    // 병원 관리자 진료기록 수정 페이지
    @GetMapping("/modify/{reservationId}")
    public String modifyForm(@PathVariable Long reservationId,
                             Authentication authentication,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        try {
            MedicalRecordResponseDTO dto =
                    medicalRecordService.prepareModifyForm(reservationId, authentication.getName());

            model.addAttribute("medicalRecordDTO", dto);
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("mode", "modify");

            return "hospitalAdmin/medicalRecords/medicalRecordForm";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/hospitalAdmin/reservations";
        }
    }

    // 병원 관리자 진료기록 수정 저장
    @PostMapping("/modify")
    public String modify(@Valid @ModelAttribute("medicalRecordDTO") MedicalRecordResponseDTO dto,
                         BindingResult bindingResult,
                         Authentication authentication,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("mode", "modify");
            return "hospitalAdmin/medicalRecords/medicalRecordForm";
        }

        try {
            medicalRecordService.modify(dto, authentication.getName());
        } catch (RuntimeException e) {
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("mode", "modify");
            model.addAttribute("error", e.getMessage());
            return "hospitalAdmin/medicalRecords/medicalRecordForm";
        }

        return "redirect:/hospitalAdmin/reservations";
    }
}
