package com.petmilyday.controller.shop;

import com.petmilyday.service.shop.ProductQnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
public class QnaApiController {

    private final ProductQnaService productQnaService;

    // 상품별 Q&A 전체 조회
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Map<String, Object>>> getProductQna(@PathVariable Long productId) {
        return ResponseEntity.ok(productQnaService.getQnaListByProduct(productId));
    }

    // 유저 문의 등록
    @PostMapping("/register")
    public ResponseEntity<String> registerQna(@RequestBody Map<String, Object> payload, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("로그인이 필요함");

        Long productId = Long.valueOf(payload.get("productId").toString());
        String content = payload.get("content").toString();

        productQnaService.registerQna(productId, principal.getName(), content);
        return ResponseEntity.ok("success");
    }

    // 어드민 전용: 답변 등록
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{qnaId}/answer")
    public ResponseEntity<String> submitAnswer(@PathVariable Long qnaId, @RequestBody Map<String, String> payload) {
        String answerContent = payload.get("answerContent");
        productQnaService.registerAnswer(qnaId, answerContent);
        return ResponseEntity.ok("success");
    }

    // 어드민 전용: 임의 삭제
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{qnaId}")
    public ResponseEntity<String> deleteQna(@PathVariable Long qnaId) {
        productQnaService.deleteQna(qnaId);
        return ResponseEntity.ok("success");
    }
}