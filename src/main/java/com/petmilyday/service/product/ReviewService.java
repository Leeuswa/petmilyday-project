package com.petmilyday.service.product;

import com.petmilyday.dto.product.ReviewWriteDto;
import com.petmilyday.entity.product.ProductReview;
import com.petmilyday.dto.product.ReviewWriteDto;
import com.petmilyday.repository.product.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ProductReviewRepository reviewRepository;
    // private final OrdersRepository ordersRepository;=

    // 특정 상품 리뷰 전체 조회
    public List<ProductReview> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    // 리뷰 등록
    @Transactional
    public ProductReview registerReview(Long memberId, ReviewWriteDto dto) {
        if (reviewRepository.existsByOrderId(dto.getOrderId())) {
            throw new IllegalStateException("이미 해당 주문 건에 대한 리뷰를 작성하셨습니다.");
        }


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