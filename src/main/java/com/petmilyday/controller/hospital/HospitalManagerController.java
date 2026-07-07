package com.petmilyday.controller.hospital;

import com.petmilyday.dto.hospital.HospitalManagerDTO;
import com.petmilyday.entity.hospital.Hospital;
import com.petmilyday.repository.hospital.HospitalRepository;
import com.petmilyday.service.hospital.HospitalManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hospital-manager")
public class HospitalManagerController {

    private final HospitalManagerService hospitalManagerService;
    private final HospitalRepository hospitalRepository;

    // 병원 관리자 신청 페이지
    @GetMapping("/request/{hospitalId}")
    public String requestPage(@PathVariable Long hospitalId, Model model) {

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("병원이 없습니다."));

        model.addAttribute("hospital", hospital);
        model.addAttribute("hospitalManagerDTO", new HospitalManagerDTO());

        return "hospital/managerRequest";
    }


    //병원 관리자 신청 처리
    @PostMapping("/request")
    public String requestManager(@ModelAttribute HospitalManagerDTO dto,
                                 Authentication authentication){

        String username = authentication.getName();

        hospitalManagerService.requestManager(username,dto);
        return "redirect:/hospital/list";
    }

}
