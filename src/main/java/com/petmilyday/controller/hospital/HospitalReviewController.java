package com.petmilyday.controller.hospital;

import com.petmilyday.dto.review.HospitalReviewRequestDTO;
import com.petmilyday.dto.review.HospitalReviewResponseDTO;
import com.petmilyday.service.hospital.HospitalReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/review")
public class HospitalReviewController {
    private final HospitalReviewService hospitalReviewService;

    @GetMapping("/list/{hospitalId}")
    public String reviewList(@PathVariable Long hospitalId, Model model){
        List<HospitalReviewResponseDTO> reviewList = hospitalReviewService.reviewList(hospitalId);
        model.addAttribute("reviewList",reviewList);
        return "review/list";
    }

    @PostMapping("/register")
    public String reviewRegister(HospitalReviewRequestDTO dto){
        hospitalReviewService.reviewRegister(dto);
        return "redirect:/hospital/"+dto.getHospitalId();
    }

}
