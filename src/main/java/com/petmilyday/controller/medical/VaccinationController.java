package com.petmilyday.controller.medical;

import com.petmilyday.dto.medical.VaccinationResponseDTO;
import com.petmilyday.service.medical.VaccinationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vaccination")
@Log4j2
public class VaccinationController {

    private final VaccinationService vaccinationService;


    @GetMapping("/list/{petId}")
    public String vaccinationList(@PathVariable Long petId, Model model){
        List<VaccinationResponseDTO> dtoList = vaccinationService.vaccinationList(petId);
        model.addAttribute("dtoList",dtoList);
        return "vaccination/list";
    }

    @GetMapping("/myVaccination")
    public String myVaccinationList(Authentication authentication,
                                    Model model){
        log.info("내 동물 예방접종 기록 조회 요청");

        List<VaccinationResponseDTO> vaccinationList =
                vaccinationService.myVaccinationList(authentication.getName());
        model.addAttribute("vaccinationList",vaccinationList);

        return "vaccination/myVaccinationList";
    }

}
