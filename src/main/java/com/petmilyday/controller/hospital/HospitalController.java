package com.petmilyday.controller.hospital;

import com.petmilyday.dto.hospital.HospitalRequestDTO;
import com.petmilyday.dto.hospital.HospitalResponseDTO;
import com.petmilyday.service.hospital.HospitalService;
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
@RequestMapping("/hospital")
public class HospitalController {

    private final HospitalService hospitalService;

    @GetMapping("/list")
    public String hospitalList(HospitalRequestDTO dto ,Model model){
        log.info("병원 목록 조회 요청 - keyword: {}, isEmergency: {}, department: {}",
                dto.getKeyword(), dto.getIsEmergency(), dto.getDepartment());
        List<HospitalResponseDTO> hospitalList = hospitalService.hospitalList(dto);
        log.info("병원 목록 조회 결과 - 총 {}개", hospitalList.size());
        model.addAttribute("hospitalList",hospitalList);
        return "hospital/hospital_list";
    }

    @GetMapping("/{hospitalId}")
    public String hospitalDetail(@PathVariable Long hospitalId, Model model){
        log.info("병원 상세 조회 요청 - hospitalId: {}", hospitalId);
        HospitalResponseDTO dto = hospitalService.hospitalReadOne(hospitalId);
        log.info("병원 상세 조회 완료 - 병원명: {}", dto.getName());
        model.addAttribute("hospital",dto);
        return "hospital/hospital_detail";
    }
}
