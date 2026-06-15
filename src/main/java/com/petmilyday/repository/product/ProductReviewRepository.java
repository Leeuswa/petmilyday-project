package com.petmilyday.repository.product;

import com.petmilyday.entity.product.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    List<ProductReview> findByProductIdOrderByCreatedAtDesc(Long productId);

    boolean existsByOrderId(Long orderId);
}