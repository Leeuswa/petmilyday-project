package com.petmilyday.service.product;

import com.petmilyday.dto.product.ReviewWriteDto;
import com.petmilyday.entity.product.ProductReview;
import com.petmilyday.repository.product.ProductReviewRepository;
import com.petmilyday.repository.shop.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ProductReviewRepository reviewRepository;
    private final OrdersRepository ordersRepository;

    // 특정 상품 리뷰 전체 조회
    public List<ProductReview> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    // 리뷰 등록
    @Transactional
    public ProductReview registerReview(String username, Long memberId, ReviewWriteDto dto) {
        // 실제 상품 구매 이력이 있는 주문 건 중 아직 리뷰를 작성하지 않은 주문을 찾는다.
        // (클라이언트가 보낸 orderId는 신뢰하지 않고, 구매 이력으로부터 직접 조회한다.)
        List<Long> orderIds = ordersRepository.findOrderIdsByUsernameAndProductId(username, dto.getProductId());
        if (orderIds.isEmpty()) {
            throw new IllegalArgumentException("해당 상품을 구매한 회원만 리뷰를 작성할 수 있습니다.");
        }

        Long orderId = orderIds.stream()
                .filter(id -> !reviewRepository.existsByOrderId(id))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("이미 해당 주문 건에 대한 리뷰를 작성하셨습니다."));

        // 리뷰 데이터 세팅 및 저장
        ProductReview review = new ProductReview();
        review.setMemberId(memberId);
        review.setProductId(dto.getProductId());
        review.setOrderId(orderId);
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setImgUrl(dto.getImgUrl());

        return reviewRepository.save(review);
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId, Long currentMemberId, boolean isAdmin) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        boolean isOwner = review.getMemberId().equals(currentMemberId);

        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException("이 리뷰를 삭제할 권한이 없습니다.");
        }

        reviewRepository.delete(review);
    }

    // 리뷰 신고
    @Transactional
    public void reportReview(Long reviewId, Long currentMemberId) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        if (review.getMemberId().equals(currentMemberId)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰는 신고할 수 없습니다.");
        }

        if (Boolean.TRUE.equals(review.getIsReported())) {
            throw new IllegalStateException("이미 신고된 리뷰입니다.");
        }

        review.report();
    }
}