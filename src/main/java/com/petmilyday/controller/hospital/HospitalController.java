package com.petmilyday.controller.hospital;

import com.petmilyday.dto.hospital.HospitalRequestDTO;
import com.petmilyday.dto.hospital.HospitalResponseDTO;
import com.petmilyday.dto.reservation.ReservationSlotDto;
import com.petmilyday.dto.review.HospitalReviewResponseDTO;
import com.petmilyday.service.hospital.HospitalReviewService;
import com.petmilyday.service.hospital.HospitalService;
import com.petmilyday.service.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/hospital")
public class HospitalController {

    private final HospitalService hospitalService;
    private final HospitalReviewService hospitalReviewService;



    @Value("${kakao.map.api-key}")
    private String kakaoMapApiKey;

    //병원 리스트 페이지 이동
    @GetMapping("/list")
    public String hospitalList(HospitalRequestDTO dto ,Model model){
        log.info("병원 목록 조회 요청 - keyword: {}, isEmergency: {}, department: {}",
                dto.getKeyword(), dto.getIsEmergency(), dto.getDepartment());
        List<HospitalResponseDTO> hospitalList = hospitalService.hospitalList(dto);
        log.info("병원 목록 조회 결과 - 총 {}개", hospitalList.size());
        model.addAttribute("hospitalList",hospitalList);
        model.addAttribute("kakaoMapApiKey", kakaoMapApiKey);
        return "hospital/hospital_list";
    }

    //병원 상세 페이지 이동
    @GetMapping("/{hospitalId}")
    public String hospitalDetail(@PathVariable Long hospitalId, Model model,
                                 Authentication authentication) {
        log.info("병원 상세 조회 요청 - hospitalId: {}", hospitalId);
        HospitalResponseDTO dto = hospitalService.hospitalReadOne(hospitalId);
        List<HospitalReviewResponseDTO> reviewList = hospitalReviewService.reviewList(hospitalId);

        Long reviewableReservationId = hospitalReviewService.findReviewableReservationId(
                hospitalId,
                authentication.getName()
        );


        model.addAttribute("hospital", dto);
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("reviewableReservationId", reviewableReservationId);
        return "hospital/hospital_detail";
    }








}
