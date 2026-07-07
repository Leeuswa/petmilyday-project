package com.petmilyday.controller.hospital;

import com.petmilyday.dto.hospital.HospitalRequestDTO;
import com.petmilyday.dto.hospital.HospitalResponseDTO;
import com.petmilyday.dto.review.HospitalReviewResponseDTO;
import com.petmilyday.service.hospital.HospitalReviewService;
import com.petmilyday.service.hospital.HospitalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String hospitalList(HospitalRequestDTO dto,
                               @RequestParam(defaultValue = "0") int page,
                               Authentication authentication,
                               Model model) {

        // /hospital/list는 비로그인 사용자도 볼 수 있는 페이지라 익명 인증("anonymousUser")인 경우를 구분해야 한다.
        boolean isLoggedIn = authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName());

        String username = isLoggedIn ? authentication.getName() : null;

        log.info("병원 목록 조회 요청 - keyword: {}, isEmergency: {}, department: {}, region: {}",
                dto.getKeyword(), dto.getIsEmergency(), dto.getDepartment(), dto.getRegion());

        Page<HospitalResponseDTO> hospitalPage = hospitalService.hospitalListPage(dto, page, username);

        log.info("병원 목록 조회 결과 - 총 {}개", hospitalPage.getTotalElements());

        model.addAttribute("hospitalPage", hospitalPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("searchDTO", dto);
        model.addAttribute("kakaoMapApiKey", kakaoMapApiKey);

        return "hospital/hospital_list";
    }

    //병원 상세 페이지 이동
    @GetMapping("/{hospitalId}")
    public String hospitalDetail(@PathVariable Long hospitalId,
                                 @RequestParam(defaultValue = "0") int reviewPage,
                                 Model model,
                                 Authentication authentication) {

        log.info("병원 상세 조회 요청 - hospitalId: {}", hospitalId);

        HospitalResponseDTO dto = hospitalService.hospitalReadOne(hospitalId);

        Page<HospitalReviewResponseDTO> reviewPaging =
                hospitalReviewService.reviewListPage(hospitalId, reviewPage);

        Long reviewableReservationId = hospitalReviewService.findReviewableReservationId(
                hospitalId,
                authentication.getName()
        );

        model.addAttribute("hospital", dto);
        model.addAttribute("reviewPage", reviewPaging);
        model.addAttribute("currentReviewPage", reviewPage);
        model.addAttribute("reviewableReservationId", reviewableReservationId);

        return "hospital/hospital_detail";
    }
}