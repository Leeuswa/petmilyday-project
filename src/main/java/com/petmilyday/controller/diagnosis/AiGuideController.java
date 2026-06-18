package com.petmilyday.controller.diagnosis;

import com.petmilyday.service.diagnosis.AiGuideService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AiGuideController {

    private final AiGuideService aiGuideService;

    // Ai 동물 가이드 이동
    @GetMapping("/ai-guide")
    public String guidePage() {
        return "diagnosis/guide";
    }

    // 사용자가 입력한 질문을 AI 가이드에 전달하고 결과를 화면에 출력
    @PostMapping("/ai-guide")
    public String askGuide(
            @RequestParam String question,
            Model model
    ) {
        String answer = aiGuideService.ask(question);

        model.addAttribute("question", question);
        model.addAttribute("answer", answer);

        return "diagnosis/guide";
    }
}