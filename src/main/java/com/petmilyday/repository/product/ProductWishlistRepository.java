package com.petmilyday.repository.product;

import com.petmilyday.entity.product.ProductWishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductWishlistRepository extends JpaRepository<ProductWishlist, Long> {

    // 특정 회원 ID와 상품 ID 기준 위시리스트(찜) 항목 조회
    Optional<ProductWishlist> findByMemberIdAndProductId(Long memberId, Long productId);

    // 특정 회원의 상품 위시리스트(찜) 등록 여부 검증
    boolean existsByMemberIdAndProductId(Long memberId, Long productId);
}