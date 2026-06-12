package com.petmilyday.service.product;

import com.petmilyday.dto.product.ReviewWriteDto;
import com.petmilyday.entity.product.ProductReview;
import com.petmilyday.repository.product.ProductReviewRepository;
import com.petmilyday.repository.shop.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ProductReviewRepository reviewRepository;
    private final OrdersRepository ordersRepository;

    // 특정 상품 리뷰 전체 조회
    public List<ProductReview> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    // 리뷰 등록
    @Transactional
    public ProductReview registerReview(String username, Long memberId, ReviewWriteDto dto) {

        // 1. [신규 추가] 실제 상품 구매 이력이 있는지 검증
        if (!ordersRepository.existsByUsernameAndProductId(username, dto.getProductId())) {
            throw new IllegalArgumentException("해당 상품을 구매한 회원만 리뷰를 작성할 수 있습니다.");
        }

        // 2. 이미 리뷰를 썼는지 검증
        if (reviewRepository.existsByOrderId(dto.getOrderId())) {
            throw new IllegalStateException("이미 해당 주문 건에 대한 리뷰를 작성하셨습니다.");
        }

        // 3. 리뷰 데이터 세팅 및 저장
        ProductReview review = new ProductReview();
        review.setMemberId(memberId);
        review.setProductId(dto.getProductId());
        review.setOrderId(dto.getOrderId());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setImgUrl(dto.getImgUrl());

        return reviewRepository.save(review);
    }
}