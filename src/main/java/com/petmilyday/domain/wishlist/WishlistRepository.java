package com.petmilyday.domain.wishlist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    boolean existsByMemberIdAndUsedPostId(Long memberId, Long usedPostId);

    void deleteByMemberIdAndUsedPostId(Long memberId, Long usedPostId);

    long countByUsedPostId(Long usedPostId);

    List<Wishlist> findByMemberId(Long memberId);

    @Query("SELECT w.usedPostId FROM Wishlist w WHERE w.memberId = :memberId")
    List<Long> findUsedPostIdsByMemberId(Long memberId);
}