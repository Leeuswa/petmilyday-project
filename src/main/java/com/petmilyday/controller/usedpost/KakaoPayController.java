package com.petmilyday.controller.usedpost;

import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.used.UsedPost;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.used.UsedPostRepository;
import com.petmilyday.service.usedpost.KakaoPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;
    private final MemberRepository memberRepository;
    private final UsedPostRepository usedPostRepository;

    // 결제 확인 페이지
    @GetMapping("/payment/confirm")
    public String confirm(
            @RequestParam Long postId,
            Model model
    ) {

        UsedPost post =
                usedPostRepository.findById(postId)
                        .orElseThrow();

        model.addAttribute("post", post);

        return "used/payment-confirm";
    }

    // 카카오페이 결제 요청
    @PostMapping("/payment/ready")
    public String ready(
            @RequestParam Long postId,
            Authentication authentication
    ) {

        String username =
                authentication.getName();

        Member member =
                memberRepository.findByUsername(username)
                        .orElseThrow();

        String redirectUrl =
                kakaoPayService.ready(
                        postId,
                        member.getId()
                );

        return "redirect:" + redirectUrl;
    }

    // 결제 취소
    @GetMapping("/payment/cancel")
    public String cancel() {

        return "redirect:/used/list";
    }

    // 결제 실패
    @GetMapping("/payment/fail")
    public String fail() {

        return "redirect:/used/list";
    }

    // 결제 승인 처리
    @GetMapping("/payment/success")
    public String success(
            @RequestParam Long postId,
            @RequestParam("pg_token") String pgToken,
            Model model
    ) {

        UsedPost post =
                kakaoPayService.approve(postId, pgToken);

        model.addAttribute("post", post);

        return "used/payment-success";
    }
}