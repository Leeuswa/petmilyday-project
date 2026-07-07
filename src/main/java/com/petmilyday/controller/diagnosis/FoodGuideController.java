package com.petmilyday.controller.diagnosis;

import com.petmilyday.entity.member.Member;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.member.PetProfileRepository;
import com.petmilyday.service.diagnosis.FoodGuideService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class FoodGuideController {

    private final FoodGuideService foodGuideService;
    private final MemberRepository memberRepository;
    private final PetProfileRepository petProfileRepository;

    // AI 사료 추천 화면
    @GetMapping("/food-guide")
    public String form(Authentication authentication, Model model) {

        if (authentication == null) {
            return "redirect:/member/login";
        }

        Member member = memberRepository.findByUsername(authentication.getName())
                .orElseThrow();

        model.addAttribute("pets", petProfileRepository.findByMember(member));

        return "diagnosis/foodGuide";
    }

    // AI 사료 추천 요청
    @PostMapping("/food-guide")
    public String recommend(
            @RequestParam Long petId,
            @RequestParam(required = false) String conditionText,
            Authentication authentication,
            Model model
    ) {

        if (authentication == null) {
            return "redirect:/member/login";
        }

        Member member = memberRepository.findByUsername(authentication.getName())
                .orElseThrow();

        model.addAttribute("pets", petProfileRepository.findByMember(member));
        model.addAttribute("selectedPetId", petId);
        model.addAttribute("conditionText", conditionText);
        model.addAttribute("answer", foodGuideService.recommend(petId, conditionText));

        return "diagnosis/foodGuide";
    }
}
