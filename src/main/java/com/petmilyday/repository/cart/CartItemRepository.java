package com.petmilyday.repository.cart;

import com.petmilyday.entity.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // 특정 장바구니에 특정 상품이 이미 담겨있는지 확인
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    List<CartItem> findAllByCartId(Long cartId);
}