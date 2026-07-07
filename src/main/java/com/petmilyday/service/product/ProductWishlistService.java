package com.petmilyday.service.product;

import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.product.Product;
import com.petmilyday.entity.product.ProductWishlist;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.product.ProductRepository;
import com.petmilyday.repository.product.ProductWishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductWishlistService {

    private final ProductWishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    // 위시리스트(찜) 토글 처리 (등록되어 있으면 삭제, 없으면 추가)
    @Transactional
    public boolean toggleWishlist(Long productId, String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        Optional<ProductWishlist> existingWish = wishlistRepository.findByMemberIdAndProductId(member.getId(), product.getId());

        if (existingWish.isPresent()) {

            wishlistRepository.delete(existingWish.get());
            return false;
        } else {

            ProductWishlist wishlist = ProductWishlist.builder()
                    .member(member)
                    .product(product)
                    .build();
            wishlistRepository.save(wishlist);
            return true;
        }
    }
}