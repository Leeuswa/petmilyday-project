package com.petmilyday.service.wishlist;

import com.petmilyday.domain.used.UsedPost;
import com.petmilyday.domain.used.UsedPostRepository;
import com.petmilyday.domain.wishlist.Wishlist;
import com.petmilyday.domain.wishlist.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UsedPostRepository usedPostRepository;

    @Override
    @Transactional
    public boolean toggle(Long memberId, Long usedPostId) {

        if (wishlistRepository.existsByMemberIdAndUsedPostId(memberId, usedPostId)) {
            wishlistRepository.deleteByMemberIdAndUsedPostId(memberId, usedPostId);
            return false;
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setMemberId(memberId);
        wishlist.setUsedPostId(usedPostId);

        wishlistRepository.save(wishlist);
        return true;
    }

    @Override
    public long count(Long usedPostId) {
        return wishlistRepository.countByUsedPostId(usedPostId);
    }

    @Override
    public boolean isWished(Long memberId, Long usedPostId) {
        return wishlistRepository.existsByMemberIdAndUsedPostId(memberId, usedPostId);
    }

    @Override
    public List<Long> getWishPostIds(Long memberId) {
        return wishlistRepository.findUsedPostIdsByMemberId(memberId);
    }

    @Override
    public Page<UsedPost> getWishPosts(Long memberId, Pageable pageable) {

        List<Long> ids = wishlistRepository.findUsedPostIdsByMemberId(memberId);

        if (ids == null || ids.isEmpty()) {
            return Page.empty(pageable);
        }

        return usedPostRepository.findWishPosts(ids, pageable);
    }
}