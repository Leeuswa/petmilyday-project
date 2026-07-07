package com.petmilyday.repository.used;

import com.petmilyday.entity.used.ItemCondition;
import com.petmilyday.entity.used.UsedPost;
import com.petmilyday.entity.used.UsedPostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsedPostRepository extends JpaRepository<UsedPost, Long> {

    // LIST + SEARCH
    @EntityGraph(attributePaths = {"member", "images"})
    @Query("""
    SELECT DISTINCT u
    FROM UsedPost u
    WHERE (:keyword IS NULL OR u.title LIKE %:keyword% OR u.content LIKE %:keyword%)
      AND (:category IS NULL OR u.category = :category)
      AND (:region IS NULL OR u.region LIKE %:region%)
      AND (:condition IS NULL OR u.itemCondition = :condition)
      AND (
            :offerAccepted IS NULL
            OR u.offerAccepted = true
          )
      AND (:minPrice IS NULL OR u.price >= :minPrice)
      AND (:maxPrice IS NULL OR u.price <= :maxPrice)
      AND u.isHidden = false
    ORDER BY
      CASE
        WHEN u.pulledUpAt IS NOT NULL AND u.pulledUpAt >= :pullUpLimit
        THEN 0
        ELSE 1
      END ASC,
      CASE
        WHEN u.pulledUpAt IS NOT NULL AND u.pulledUpAt >= :pullUpLimit
        THEN u.pulledUpAt
        ELSE u.createdAt
      END DESC
    """)
    Page<UsedPost> searchList(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("region") String region,
            @Param("condition") ItemCondition condition,
            @Param("offerAccepted") Boolean offerAccepted,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("pullUpLimit") LocalDateTime pullUpLimit,
            Pageable pageable
    );

    // 찜 목록 페이징
    @EntityGraph(attributePaths = {"member", "images"})
    @Query("""
            SELECT DISTINCT u
            FROM UsedPost u
            WHERE u.id IN :ids
              AND u.isHidden = false
            ORDER BY u.createdAt DESC
            """)
    Page<UsedPost> findWishPosts(
            @Param("ids") List<Long> ids,
            Pageable pageable
    );

    // DETAIL
    @EntityGraph(attributePaths = {"member", "images"})
    @Query("""
            SELECT u
            FROM UsedPost u
            WHERE u.id = :id
              AND u.isHidden = false
            """)
    UsedPost findDetail(@Param("id") Long id);

    // STATUS UPDATE
    @Modifying
    @Query("""
            UPDATE UsedPost u
            SET u.status = :status
            WHERE u.id = :id
            """)
    void updateStatus(@Param("id") Long id,
                      @Param("status") UsedPostStatus status);

    // SOFT DELETE
    @Modifying
    @Query("""
            UPDATE UsedPost u
            SET u.isHidden = true
            WHERE u.id = :id
            """)
    void softDelete(@Param("id") Long id);

    Optional<UsedPost> findByPaymentKey(String paymentKey);

    Optional<UsedPost> findTopByOrderByIdDesc();

    // 찜 목록 DTO용
    @EntityGraph(attributePaths = {"member", "images"})
    @Query("""
            SELECT DISTINCT u
            FROM UsedPost u
            WHERE u.id IN :ids
              AND u.isHidden = false
            ORDER BY u.createdAt DESC
            """)
    List<UsedPost> findWishPostsForDto(
            @Param("ids") List<Long> ids
    );
}