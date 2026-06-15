package com.petmilyday.controller.hospital;

import com.petmilyday.dto.medical.MedicalRecordResponseDTO;
import com.petmilyday.service.medical.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hospitalAdmin/medicalRecords")
public class HospitalAdminMedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    // 병원 관리자 진료기록 작성 페이지
    @GetMapping("/register/{reservationId}")
    public String registerForm(@PathVariable Long reservationId,
                               Model model) {

        MedicalRecordResponseDTO dto = MedicalRecordResponseDTO.builder()
                .reservationId(reservationId)
                .visitDate(LocalDate.now())
                .build();

        model.addAttribute("medicalRecordDTO", dto);

        return "hospitalAdmin/medicalRecords/medicalRecordForm";
    }

    // 병원 관리자 진료기록 저장
    @PostMapping("/register")
    public String register(@ModelAttribute MedicalRecordResponseDTO dto) {

        medicalRecordService.register(dto);

        return "redirect:/hospitalAdmin/reservations";
    }
}