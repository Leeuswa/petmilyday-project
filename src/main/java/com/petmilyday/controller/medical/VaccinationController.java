package com.petmilyday.controller.medical;

import com.petmilyday.dto.medical.VaccinationResponseDTO;
import com.petmilyday.service.medical.VaccinationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

}
