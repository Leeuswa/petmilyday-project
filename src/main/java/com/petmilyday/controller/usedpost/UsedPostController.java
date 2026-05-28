package com.petmilyday.controller.usedpost;

import com.petmilyday.entity.used.ItemCondition;
import com.petmilyday.entity.used.UsedPost;
import com.petmilyday.entity.used.UsedPostStatus;
import com.petmilyday.dto.usedpost.UsedPostDTO;
import com.petmilyday.service.usedpost.UsedPostService;
import com.petmilyday.service.wishlist.WishlistService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UsedPostController {

    private final UsedPostService usedPostService;
    private final WishlistService wishlistService;

    // =========================
    // ItemCondition 변환
    // =========================
    private ItemCondition parseCondition(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return ItemCondition.valueOf(value.toUpperCase());

        } catch (IllegalArgumentException e) {

            return null;
        }
    }

    // =========================
    // UsedPostStatus 변환
    // =========================
    private UsedPostStatus parseStatus(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return UsedPostStatus.valueOf(value.toUpperCase());

        } catch (IllegalArgumentException e) {

            return null;
        }
    }

    // =========================
    // LIST
    // =========================
    @GetMapping("/used/list")
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) ItemCondition condition,
            @RequestParam(required = false) Boolean offerAccepted,
            @RequestParam(required = false, defaultValue = "false") boolean onlyWish,
            @SessionAttribute(name = "memberId", required = false) Long memberId,
            Pageable pageable,
            Model model
    ) {

        // =========================
        // 찜 목록
        // =========================
        if (onlyWish) {

            Page<UsedPost> result =
                    usedPostService.getWishPosts(memberId, pageable);

            model.addAttribute("posts", result.map(UsedPostDTO::new));

            model.addAttribute("keyword", keyword);
            model.addAttribute("category", category);
            model.addAttribute("region", region);
            model.addAttribute("condition", condition);
            model.addAttribute("offerAccepted", offerAccepted);
            model.addAttribute("onlyWish", true);

            return "used/list";
        }

        // =========================
        // 가격제안 필터 (중요: true만 필터)
        // =========================
        Boolean filterOffer = Boolean.TRUE.equals(offerAccepted) ? true : null;

        Page<UsedPost> result = usedPostService.searchList(
                keyword,
                category,
                region,
                condition,
                filterOffer,
                pageable
        );

        model.addAttribute("posts", result.map(UsedPostDTO::new));

        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("region", region);
        model.addAttribute("condition", condition);
        model.addAttribute("offerAccepted", offerAccepted);
        model.addAttribute("onlyWish", onlyWish);

        return "used/list";
    }

    // =========================
// WRITE
// =========================
    @PostMapping("/used/write")
    public String write(
            @ModelAttribute UsedPostDTO dto,
            @RequestParam(value = "images", required = false) List<MultipartFile> files,
            HttpSession session
    ) throws IOException {

        Long loginMemberId = (Long) session.getAttribute("memberId");

        if (loginMemberId == null) {
            return "redirect:/used/list?loginRequired=true";
        }

        usedPostService.write(dto, files, loginMemberId);

        return "redirect:/used/list";
    }

    // =========================
    // DETAIL
    // =========================
    @GetMapping("/used/detail/{id}")
    public String detail(

            @PathVariable Long id,
            Model model,
            HttpSession session
    ) {

        UsedPostDTO post =
                usedPostService.getDetail(id);

        if (post == null
                || Boolean.TRUE.equals(post.getIsHidden())) {

            return "redirect:/used/list";
        }

        Long memberId =
                (Long) session.getAttribute("memberId");

        boolean isWriter =
                memberId != null
                        && memberId.equals(post.getMemberId());

        boolean wished =
                memberId != null
                        && wishlistService.isWished(memberId, id);

        model.addAttribute("post", post);
        model.addAttribute("isWriter", isWriter);
        model.addAttribute("wished", wished);

        return "used/detail";
    }

    // =========================
    // REPORT
    // =========================
    @PostMapping("/used/report/{id}")
    public String report(

            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam(required = false) String content
    ) {

        usedPostService.reportPost(id);

        return "redirect:/used/detail/" + id;
    }

    // =========================
    // EDIT FORM
    // =========================
    @GetMapping("/used/edit/{id}")
    public String editForm(

            @PathVariable Long id,
            Model model
    ) {

        UsedPostDTO post =
                usedPostService.getDetail(id);

        if (post == null
                || Boolean.TRUE.equals(post.getIsHidden())) {

            return "redirect:/used/list";
        }

        model.addAttribute("post", post);

        return "used/edit";
    }

    // =========================
    // EDIT
    // =========================
    @PostMapping("/used/edit/{id}")
    public String edit(

            @PathVariable Long id,
            @ModelAttribute UsedPostDTO dto
    ) {

        usedPostService.edit(id, dto);

        return "redirect:/used/detail/" + id;
    }

    // =========================
    // STATUS 변경
    // =========================
    @PostMapping("/used/status/{id}")
    public String changeStatus(

            @PathVariable Long id,
            @RequestParam String status
    ) {

        UsedPostStatus statusEnum =
                parseStatus(status);

        if (statusEnum != null) {

            usedPostService.changeStatus(
                    id,
                    statusEnum
            );
        }

        return "redirect:/used/detail/" + id;
    }

    // =========================
    // 판매 완료
    // =========================
    @PostMapping("/used/complete/{id}")
    public String completeSale(
            @PathVariable Long id
    ) {

        usedPostService.completeSale(id);

        return "redirect:/used/detail/" + id;
    }

    // =========================
    // 찜 목록
    // =========================
    @GetMapping("/wishlist/list")
    public String wishList(

            HttpSession session,
            Model model
    ) {

        Long memberId =
                (Long) session.getAttribute("memberId");

        if (memberId == null) {

            return "redirect:/used/list";
        }

        List<Long> wishIds =
                wishlistService.getWishPostIds(memberId);

        List<UsedPostDTO> wishPosts =
                usedPostService.getWishList(wishIds);

        model.addAttribute("posts", wishPosts);

        return "wishlist/list";
    }
}