package com.petmilyday.controller.hospital;

import com.petmilyday.dto.hospital.HospitalAdminHospitalDTO;
import com.petmilyday.service.hospital.HospitalAdminHospitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hospitalAdmin/hospital")
public class HospitalAdminHospitalController {

    private final HospitalAdminHospitalService hospitalAdminHospitalService;

    //병원 상세
    @GetMapping
    public String hospitalDetail(Authentication authentication,
                                 Model model) {

        String username = authentication.getName();

        HospitalAdminHospitalDTO hospitalDTO =
                hospitalAdminHospitalService.findMyHospital(username);

        model.addAttribute("hospitalDTO", hospitalDTO);

        return "hospitalAdmin/hospital/hospitalDetail";
    }

    //병원 수정
    @GetMapping("/modify")
    public String hospitalModifyForm(Authentication authentication,
                                     Model model) {

        String username = authentication.getName();

        HospitalAdminHospitalDTO hospitalDTO =
                hospitalAdminHospitalService.findMyHospital(username);

        model.addAttribute("hospitalDTO", hospitalDTO);

        return "hospitalAdmin/hospital/hospitalModify";
    }

    @PostMapping("/modify")
    public String hospitalModify(Authentication authentication,
                                 @Valid @ModelAttribute("hospitalDTO") HospitalAdminHospitalDTO dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "hospitalAdmin/hospital/hospitalModify";
        }

        String username = authentication.getName();

        hospitalAdminHospitalService.modifyMyHospital(username, dto);

        redirectAttributes.addFlashAttribute("message", "병원 세부정보가 수정되었습니다.");

        return "redirect:/hospitalAdmin/hospital";
    }
}