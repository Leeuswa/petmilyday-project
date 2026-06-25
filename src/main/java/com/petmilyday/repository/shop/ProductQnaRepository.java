package com.petmilyday.repository.shop;

import com.petmilyday.entity.product.ProductQna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductQnaRepository extends JpaRepository<ProductQna, Long> {
    // 상품별 Q&A 최신순 조회
    List<ProductQna> findByProductIdOrderByCreatedAtDesc(Long productId);

    // 메인 관리자 - 전체 Q&A 목록 + 상태/키워드(상품명·작성자·문의내용) 검색 + 페이징
    @Query(
            value = """
                select q
                from ProductQna q
                join fetch q.product p
                join fetch q.member m
                where (:status is null or :status = '' or q.status = :status)
                  and (:keyword is null or :keyword = ''
                       or p.name like concat('%', :keyword, '%')
                       or m.nickname like concat('%', :keyword, '%')
                       or q.content like concat('%', :keyword, '%'))
                order by q.createdAt desc
            """,
            countQuery = """
                select count(q)
                from ProductQna q
                join q.product p
                join q.member m
                where (:status is null or :status = '' or q.status = :status)
                  and (:keyword is null or :keyword = ''
                       or p.name like concat('%', :keyword, '%')
                       or m.nickname like concat('%', :keyword, '%')
                       or q.content like concat('%', :keyword, '%'))
            """
    )
    Page<ProductQna> searchForAdmin(
            @Param("status") String status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}