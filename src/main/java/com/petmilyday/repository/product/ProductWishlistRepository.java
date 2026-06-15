package com.petmilyday.repository.product;

import com.petmilyday.entity.product.ProductWishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductWishlistRepository extends JpaRepository<ProductWishlist, Long> {
    // 🎯 특정 회원이 특정 상품을 이미 찜했는지 확인하는 메서드
    Optional<ProductWishlist> findByMemberIdAndProductId(Long memberId, Long productId);

    // 🎯 이미 찜했는지 유무 체크용
    boolean existsByMemberIdAndProductId(Long memberId, Long productId);
}