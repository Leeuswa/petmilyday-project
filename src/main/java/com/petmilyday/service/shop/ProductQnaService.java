package com.petmilyday.service.shop;

import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.product.Product;
import com.petmilyday.entity.product.ProductQna;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.product.ProductRepository;
import com.petmilyday.repository.shop.ProductQnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQnaService {

    private final ProductQnaRepository productQnaRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    // 상품별 Q&A 목록 조회 (프론트 데이터 전송용)
    public List<Map<String, Object>> getQnaListByProduct(Long productId) {
        List<ProductQna> qnas = productQnaRepository.findByProductIdOrderByCreatedAtDesc(productId);
        return qnas.stream().map(q -> Map.<String, Object>of(
                "id", q.getId(),
                "content", q.getContent(),
                "answer", q.getAnswer() != null ? q.getAnswer() : "",
                "status", q.getStatus(),
                "nickname", q.getMember().getNickname(),
                "createdAt", q.getCreatedAt().toString()
        )).collect(Collectors.toList());
    }

    // 일반 유저 문의 등록
    @Transactional
    public void registerQna(Long productId, String username, String content) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품"));
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저"));

        ProductQna qna = ProductQna.builder()
                .product(product)
                .member(member)
                .content(content)
                .build();

        productQnaRepository.save(qna);
    }

    // 어드민 전용: 답변 등록
    @Transactional
    public void registerAnswer(Long qnaId, String answerContent) {
        ProductQna qna = productQnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의"));
        qna.setAnswer(answerContent);
        qna.setStatus("ANSWERED");
    }

    // 어드민 전용: 임의 삭제
    @Transactional
    public void deleteQna(Long qnaId) {
        productQnaRepository.deleteById(qnaId);
    }
}