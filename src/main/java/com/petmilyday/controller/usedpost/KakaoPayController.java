package com.petmilyday.controller.usedpost;

import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.used.UsedPost;
import com.petmilyday.entity.used.UsedPostStatus;
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

    // 결제 시작
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

    @GetMapping("/payment/test-success")
    public String testSuccess(
            @RequestParam Long postId,
            Authentication authentication,
            Model model
    ) {

        if (authentication == null) {
            return "redirect:/member/login";
        }

        String username = authentication.getName();

        Member member =
                memberRepository.findByUsername(username)
                        .orElseThrow();

        UsedPost post =
                usedPostRepository.findById(postId)
                        .orElseThrow();

        post.setBuyerId(member.getId());
        post.setStatus(UsedPostStatus.SOLD);

        if (post.getPaymentKey() == null || post.getPaymentKey().isBlank()) {
            post.setPaymentKey("TEST-" + System.currentTimeMillis());
        }

        usedPostRepository.save(post);

        model.addAttribute("post", post);

        return "used/payment-success";
    }
}