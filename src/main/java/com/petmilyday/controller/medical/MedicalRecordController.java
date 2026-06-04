package com.petmilyday.controller.medical;

import com.petmilyday.dto.medical.MedicalRecordResponseDTO;
import com.petmilyday.service.medical.MedicalRecordService;
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
@Log4j2
@RequestMapping("/medical")
public class MedicalRecordController {
    private final MedicalRecordService medicalRecordService;


    @GetMapping("/record/{petId}")
    public String medicalRecordGet(@PathVariable Long petId, Model model){
        List<MedicalRecordResponseDTO> list = medicalRecordService.medicalRecordList(petId);
        model.addAttribute("medicalRecordList",list);

        return "medical/record";
    }

}
