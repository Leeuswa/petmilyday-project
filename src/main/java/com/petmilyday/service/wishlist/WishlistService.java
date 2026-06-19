package com.petmilyday.service.wishlist;

import com.petmilyday.entity.used.UsedPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WishlistService {

    boolean toggle(Long memberId, Long usedPostId);

    long count(Long usedPostId);

    boolean isWished(Long memberId, Long usedPostId);

    List<Long> getWishPostIds(Long memberId);

    Page<UsedPost> getWishPosts(Long memberId, Pageable pageable);
}