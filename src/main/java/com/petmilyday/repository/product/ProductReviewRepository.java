package com.petmilyday.repository.product;

import com.petmilyday.entity.product.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    // 특정 상품의 리뷰 목록을 최신순으로 조회
    List<ProductReview> findByProductIdOrderByCreatedAtDesc(Long productId);

    // 특정 주문 ID의 리뷰 작성 여부 검증
    boolean existsByOrderId(Long orderId);
}