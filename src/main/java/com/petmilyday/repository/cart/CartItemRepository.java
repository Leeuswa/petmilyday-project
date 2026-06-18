package com.petmilyday.repository.cart;

import com.petmilyday.entity.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 장바구니 ID와 상품 ID 기준 특정 상품 항목 조회
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    // 특정 장바구니에 담긴 모든 상품 항목 목록 조회
    List<CartItem> findAllByCartId(Long cartId);
}