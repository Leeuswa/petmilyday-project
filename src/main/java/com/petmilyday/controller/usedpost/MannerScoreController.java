package com.petmilyday.controller.usedpost;

import com.petmilyday.entity.member.Member;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.service.usedpost.MannerScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class MannerScoreController {

    private final MannerScoreService mannerScoreService;
    private final MemberRepository memberRepository;

    @GetMapping("/manner/evaluate")
    public String evaluatePage(
            @RequestParam Long postId,
            @RequestParam Long targetMemberId,
            Model model
    ) {

        model.addAttribute("postId", postId);
        model.addAttribute("targetMemberId", targetMemberId);

        return "used/manner-evaluate";
    }

    @PostMapping("/manner/evaluate")
    public String evaluate(
            @RequestParam Long postId,
            @RequestParam Long targetMemberId,
            @RequestParam Integer score,
            Authentication authentication
    ) {

        if (authentication == null) {
            return "redirect:/member/login";
        }

        String username = authentication.getName();

        Member fromMember =
                memberRepository.findByUsername(username)
                        .orElseThrow();

        mannerScoreService.evaluate(
                fromMember.getId(),
                targetMemberId,
                postId,
                score
        );

        return "redirect:/chat/list";
    }
}