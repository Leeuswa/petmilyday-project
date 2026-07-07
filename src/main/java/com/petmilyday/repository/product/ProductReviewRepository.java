package com.petmilyday.repository.product;

import com.petmilyday.entity.product.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    // 특정 상품의 리뷰 목록을 최신순으로 조회
    List<ProductReview> findByProductIdOrderByCreatedAtDesc(Long productId);

    // 특정 주문 ID의 리뷰 작성 여부 검증
    boolean existsByOrderId(Long orderId);

    // 메인 관리자 - 전체 상품 리뷰 조회 + 키워드(상품명·작성자·리뷰내용) 검색 + 페이징 (신고 여부 무관)
    // ProductReview는 Product/Member를 연관관계가 아닌 ID(productId/memberId)로만 갖고 있어 join 대신 id로 직접 매칭한다.
    @Query(
            value = """
                select r
                from ProductReview r, Product p, Member m
                where r.productId = p.id
                  and r.memberId = m.id
                  and (:keyword is null or :keyword = ''
                       or p.name like concat('%', :keyword, '%')
                       or m.nickname like concat('%', :keyword, '%')
                       or r.content like concat('%', :keyword, '%'))
                order by r.createdAt desc
            """,
            countQuery = """
                select count(r)
                from ProductReview r, Product p, Member m
                where r.productId = p.id
                  and r.memberId = m.id
                  and (:keyword is null or :keyword = ''
                       or p.name like concat('%', :keyword, '%')
                       or m.nickname like concat('%', :keyword, '%')
                       or r.content like concat('%', :keyword, '%'))
            """
    )
    Page<ProductReview> searchReviewsForAdmin(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}