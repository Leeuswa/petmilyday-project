package com.petmilyday.repository.shop;

import com.petmilyday.entity.product.ProductQna;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductQnaRepository extends JpaRepository<ProductQna, Long> {
    // 상품별 Q&A 최신순 조회
    List<ProductQna> findByProductIdOrderByCreatedAtDesc(Long productId);
}