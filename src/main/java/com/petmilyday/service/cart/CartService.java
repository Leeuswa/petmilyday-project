package com.petmilyday.service.cart;

import com.petmilyday.dto.cart.CartItemResponseDto;
import com.petmilyday.dto.cart.CartRequestDto;
import com.petmilyday.entity.cart.Cart;
import com.petmilyday.entity.cart.CartItem;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.product.Product;
import com.petmilyday.repository.cart.CartItemRepository;
import com.petmilyday.repository.cart.CartRepository;
import com.petmilyday.repository.member.MemberRepository;
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
    private final MemberRepository memberRepository;

    // Long userId 대신 String username을 받아서 진짜 회원 ID 매핑
    @Transactional
    public void addCart(String username, CartRequestDto requestDto) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Long userId = member.getId();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(new Cart(userId)));

        // 2. 담으려는 상품이 실제 존재하는지 검증
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

    // 장바구니 목록 조회도 로그인한 유저네임 기반으로 변경
    public List<CartItemResponseDto> getCartItems(String username) {
        Member member = memberRepository.findByUsername(username).orElse(null);
        if (member == null) return List.of();

        Cart cart = cartRepository.findByUserId(member.getId()).orElse(null);
        if (cart == null) return List.of();

        return cartItemRepository.findAllByCartId(cart.getId()).stream()
                .map(CartItemResponseDto::new).toList();
    }

    // 수량 변경
    @Transactional
    public void updateQuantity(Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템 없음"));
        cartItem.updateQuantity(quantity);
    }

    // 아이템 삭제
    @Transactional
    public void deleteCartItems(List<Long> cartItemIds) {
        cartItemRepository.deleteAllById(cartItemIds);
    }

    // 장바구니 상단 카운트 조회도 유저네임 기반으로 변경
    @Transactional(readOnly = true)
    public int getCartItemCount(String username) {
        Member member = memberRepository.findByUsername(username).orElse(null);
        if (member == null) return 0;

        Cart cart = cartRepository.findByUserId(member.getId()).orElse(null);
        if (cart == null) return 0;

        return cart.getCartItems().size();
    }
}