package com.petmilyday.controller.diagnosis;

import com.petmilyday.entity.diagnosis.DiagnosisHistory;
import com.petmilyday.entity.member.Member;
import com.petmilyday.repository.diagnosis.DiagnosisHistoryRepository;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.member.PetProfileRepository;
import com.petmilyday.service.diagnosis.DiagnosisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class DiagnosisController {

    private final DiagnosisService diagnosisService;
    private final MemberRepository memberRepository;
    private final PetProfileRepository petProfileRepository;
    private final DiagnosisHistoryRepository diagnosisHistoryRepository;

    // AI 자가진단 메인
    @GetMapping("/ai-diagnosis")
    public String diagnosisMain(
            Authentication authentication,
            Model model
    ) {

        if (authentication != null) {

            Member member =
                    memberRepository.findByUsername(authentication.getName())
                            .orElseThrow();

            Page<DiagnosisHistory> histories =
                    diagnosisService.getHistory(
                            member.getId(),
                            PageRequest.of(0, 3)
                    );

            model.addAttribute("recentHistories", histories.getContent());

            model.addAttribute(
                    "totalDiagnosisCount",
                    histories.getTotalElements()
            );

            if (!histories.getContent().isEmpty()) {

                model.addAttribute(
                        "lastDiagnosisDate",
                        histories.getContent()
                                .get(0)
                                .getCreatedAt()
                );
            }
        }

        return "diagnosis/main";
    }

    // AI 자가진단 화면
    @GetMapping("/diagnosis")
    public String form(
            Authentication authentication,
            Model model
    ) {

        if (authentication == null) {
            return "redirect:/member/login";
        }

        Member member =
                memberRepository.findByUsername(authentication.getName())
                        .orElseThrow();

        model.addAttribute("pets", petProfileRepository.findByMember(member));

        return "diagnosis/form";
    }

    // AI 자가진단 실행
    @PostMapping("/diagnosis")
    public String diagnose(
            @RequestParam Long petId,
            @RequestParam(required = false) String symptomText,
            @RequestParam(required = false) MultipartFile image,
            Authentication authentication,
            Model model
    ) throws Exception {

        if (authentication == null) {
            return "redirect:/member/login";
        }

        Member member =
                memberRepository.findByUsername(authentication.getName())
                        .orElseThrow();

        DiagnosisHistory result =
                diagnosisService.diagnose(
                        member.getId(),
                        petId,
                        symptomText,
                        image
                );

        model.addAttribute("result", result);

        return "diagnosis/result";
    }

    // AI 자가진단 이력
    @GetMapping("/diagnosis/history")
    public String history(
            Authentication authentication,
            @PageableDefault(size = 5)
            Pageable pageable,
            Model model
    ) {

        if (authentication == null) {
            return "redirect:/member/login";
        }

        Member member =
                memberRepository.findByUsername(authentication.getName())
                        .orElseThrow();

        Page<DiagnosisHistory> histories =
                diagnosisService.getHistory(member.getId(), pageable);

        int page = pageable.getPageNumber();

        int startPage = Math.max(0, page - 4);
        int endPage = Math.min(
                histories.getTotalPages() - 1,
                page + 4
        );

        model.addAttribute("histories", histories);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("currentPage", page);

        return "diagnosis/history";
    }


}