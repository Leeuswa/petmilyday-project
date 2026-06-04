package com.petmilyday.controller.hospital;

import com.petmilyday.dto.review.HospitalReviewRequestDTO;
import com.petmilyday.dto.review.HospitalReviewResponseDTO;
import com.petmilyday.service.hospital.HospitalReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/review")
public class HospitalReviewController {
    private final HospitalReviewService hospitalReviewService;

    //병원의 리뷰 리스트 페이지
    @GetMapping("/list/{hospitalId}")
    public String reviewList(@PathVariable Long hospitalId, Model model){
        List<HospitalReviewResponseDTO> reviewList = hospitalReviewService.reviewList(hospitalId);
        model.addAttribute("reviewList",reviewList);
        return "review/list";
    }

    //리뷰 작성 페이지
    @PostMapping("/register")
    public String reviewRegister(@Valid HospitalReviewRequestDTO dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Authentication authentication){

        //DTO에서 검증값에서 문제가 생겼을 경우
      if (bindingResult.hasErrors()){
        redirectAttributes.addFlashAttribute(
                "error",
                bindingResult.getFieldError().getDefaultMessage());
        return  "redirect:/hospital/" + dto.getHospitalId();
      }

        //서비스 로직에서 문제가 생겼을 경우
      try {
          hospitalReviewService.reviewRegister(dto,authentication.getName());
      }catch (RuntimeException e){
          redirectAttributes.addFlashAttribute(
                  "error",
                  e.getMessage());
          return "redirect:/hospital/"+dto.getHospitalId();
      }

        return "redirect:/hospital/"+dto.getHospitalId();
    }

    //리뷰 수정
    @PostMapping("/modify/{reviewId}")
    public String reviewModify(
            @PathVariable Long reviewId,
          @Valid  HospitalReviewRequestDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Authentication authentication){
        if(bindingResult.hasErrors()){

            redirectAttributes.addFlashAttribute(
                    "error",
                    bindingResult.getFieldError().getDefaultMessage());

            return "redirect:/hospital/" + dto.getHospitalId();
        }

        try {
            hospitalReviewService.reviewModify(reviewId,dto,authentication.getName());
        }catch (RuntimeException e){
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage());
            return "redirect:/hospital/" + dto.getHospitalId();
        }


        return "redirect:/hospital/" + dto.getHospitalId();
    }

    // 리뷰 삭제
    @PostMapping("/remove/{reviewId}")
    public String reviewRemove(@PathVariable Long reviewId,
                               Long hospitalId,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {

        try {
            hospitalReviewService.reviewRemove(
                    reviewId,
                    authentication.getName()
            );
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/hospital/" + hospitalId;
    }

    //마이페이지에 내가 쓴 병원 리뷰
    @GetMapping("/myReview")
    public String myReviewList(Authentication authentication,
                               Model model){
        List<HospitalReviewResponseDTO> reviewList =
                hospitalReviewService.myReivewList(authentication.getName());

        model.addAttribute("reviewList",reviewList);
        return "review/myReviewList";

    }
}
