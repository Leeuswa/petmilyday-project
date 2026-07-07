package com.petmilyday.controller.hospital;

import com.petmilyday.dto.review.HospitalReviewRequestDTO;
import com.petmilyday.dto.review.HospitalReviewResponseDTO;
import com.petmilyday.service.hospital.HospitalReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/review")
public class HospitalReviewController {

    private final HospitalReviewService hospitalReviewService;

    // 병원의 리뷰 리스트 페이지
    @GetMapping("/list/{hospitalId}")
    public String reviewList(@PathVariable Long hospitalId,
                             @RequestParam(defaultValue = "0") int page,
                             Model model) {

        Page<HospitalReviewResponseDTO> reviewPage =
                hospitalReviewService.reviewListPage(hospitalId, page);

        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("hospitalId", hospitalId);

        return "review/list";
    }

    // 리뷰 작성
    @PostMapping("/register")
    public String reviewRegister(@Valid HospitalReviewRequestDTO dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Authentication authentication) {

        // DTO 검증 오류
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    bindingResult.getFieldError().getDefaultMessage()
            );

            return "redirect:/hospital/" + dto.getHospitalId();
        }

        // 서비스 로직 오류
        try {
            hospitalReviewService.reviewRegister(dto, authentication.getName());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/hospital/" + dto.getHospitalId();
        }

        return "redirect:/hospital/" + dto.getHospitalId();
    }

    // 리뷰 수정
    @PostMapping("/modify/{reviewId}")
    public String reviewModify(@PathVariable Long reviewId,
                               @Valid HospitalReviewRequestDTO dto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Authentication authentication) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    bindingResult.getFieldError().getDefaultMessage()
            );

            return "redirect:/hospital/" + dto.getHospitalId();
        }

        try {
            hospitalReviewService.reviewModify(
                    reviewId,
                    dto,
                    authentication.getName()
            );
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

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

    // 리뷰 신고
    @PostMapping("/report/{reviewId}")
    public String reviewReport(@PathVariable Long reviewId,
                               @RequestParam(required = false) Long hospitalId,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {

        try {
            Long reportedHospitalId = hospitalReviewService.reportReview(reviewId, authentication.getName());

            redirectAttributes.addFlashAttribute(
                    "message",
                    "리뷰가 신고되었습니다."
            );

            return "redirect:/hospital/" + reportedHospitalId;

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return hospitalId != null
                    ? "redirect:/review/list/" + hospitalId
                    : "redirect:/hospital/list";
        }
    }

    // 마이페이지에 내가 쓴 병원 리뷰
    @GetMapping("/myReview")
    public String myReviewList(Authentication authentication,
                               @RequestParam(defaultValue = "0") int page,
                               Model model) {

        Page<HospitalReviewResponseDTO> reviewPage =
                hospitalReviewService.myReivewListPage(authentication.getName(), page);

        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("currentPage", page);

        return "review/myReviewList";
    }
}