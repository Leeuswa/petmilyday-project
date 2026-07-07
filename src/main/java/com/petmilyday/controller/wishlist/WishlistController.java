package com.petmilyday.controller.wishlist;

import com.petmilyday.service.wishlist.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    // 찜 토글
    @PostMapping("/toggle")
    public Map<String, Object> toggle(@RequestParam Long memberId,
                                      @RequestParam Long usedPostId) {

        boolean result = wishlistService.toggle(memberId, usedPostId);

        Map<String, Object> response = new HashMap<>();
        response.put("wished", result);

        return response;
    }

    // 개수
    @GetMapping("/count")
    public long count(@RequestParam Long usedPostId) {
        return wishlistService.count(usedPostId);
    }

    // 체크
    @GetMapping("/check")
    public boolean check(@RequestParam Long memberId,
                         @RequestParam Long usedPostId) {
        return wishlistService.isWished(memberId, usedPostId);
    }
}