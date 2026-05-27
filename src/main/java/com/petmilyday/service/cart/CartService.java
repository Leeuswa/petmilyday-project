package com.petmilyday.service.cart;

import com.petmilyday.dto.cart.CartItemResponseDto;
import com.petmilyday.dto.cart.CartRequestDto;
import com.petmilyday.entity.cart.Cart;
import com.petmilyday.entity.cart.CartItem;
import com.petmilyday.entity.product.Product;
import com.petmilyday.repository.cart.CartItemRepository;
import com.petmilyday.repository.cart.CartRepository;
import com.petmilyday.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void addCart(Long userId, CartRequestDto requestDto) {
        // [수정된 부분] findById -> findByUserId 로 변경!
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(new Cart(userId)));

        // 2. 담으려는 상품이 실제 존재하는지 검증 (아래는 기존과 동일)
        Product product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        // 3. 해당 장바구니에 이 상품이 이미 들어있는지 조회
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (cartItem != null) {
            cartItem.addQuantity(requestDto.getQuantity());
        } else {
            cartItem = new CartItem(cart, product, requestDto.getQuantity());
            cartItemRepository.save(cartItem);
        }
    }
    public List<CartItemResponseDto> getCartItems(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) return List.of();

        return cartItemRepository.findAllByCartId(cart.getId()).stream()
                .map(CartItemResponseDto::new).toList();
    }

    // [★추가] 수량 변경 (+, - 버튼)
    @Transactional
    public void updateQuantity(Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템 없음"));
        cartItem.updateQuantity(quantity);
    }

    // [★추가] 아이템 삭제 (X 버튼, 선택 삭제)
    @Transactional
    public void deleteCartItems(List<Long> cartItemIds) {
        cartItemRepository.deleteAllById(cartItemIds);
    }
    @Transactional(readOnly = true)
    public int getCartItemCount(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) return 0;

        // 장바구니에 담긴 아이템 리스트의 사이즈(종류 수)를 반환
        return cart.getCartItems().size();
    }
}