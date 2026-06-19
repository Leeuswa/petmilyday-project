package com.petmilyday.repository.shop;

import com.petmilyday.entity.product.ProductQna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductQnaRepository extends JpaRepository<ProductQna, Long> {
    // 상품별 Q&A 최신순 조회
    List<ProductQna> findByProductIdOrderByCreatedAtDesc(Long productId);

    // 메인 관리자 - 전체 Q&A 목록
    @Query(
            value = """
                select q
                from ProductQna q
                join fetch q.product p
                join fetch q.member m
                order by q.createdAt desc
            """,
            countQuery = """
                select count(q)
                from ProductQna q
            """
    )
    Page<ProductQna> findAllForAdmin(Pageable pageable);
}