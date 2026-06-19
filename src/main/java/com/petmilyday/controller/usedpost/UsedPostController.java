package com.petmilyday.controller.usedpost;

import com.petmilyday.dto.usedpost.UsedPostDTO;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.used.ItemCondition;
import com.petmilyday.entity.used.UsedPost;
import com.petmilyday.entity.used.UsedPostStatus;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.used.UsedPostReportRepository;
import com.petmilyday.repository.used.UsedPostRepository;
import com.petmilyday.service.usedpost.UsedPostService;
import com.petmilyday.service.wishlist.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UsedPostController {

    private final UsedPostService usedPostService;
    private final WishlistService wishlistService;
    private final MemberRepository memberRepository;
    private final UsedPostRepository usedPostRepository;
    private final UsedPostReportRepository usedPostReportRepository;

    // UsedPostStatus 변환
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

    // 페이징 값 설정
    private void addPagingAttributes(
            Model model,
            Page<?> result,
            Pageable pageable
    ) {

        int page = pageable.getPageNumber();

        int startPage = Math.max(0, page - 4);

        int endPage =
                result.getTotalPages() > 0
                        ? Math.min(result.getTotalPages() - 1, page + 4)
                        : 0;

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("currentPage", page);
    }

    // 중고거래 목록 이동
    @GetMapping("/used")
    public String usedRedirect() {
        return "redirect:/used/list";
    }

    // 중고거래 목록 조회
    @GetMapping("/used/list")
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) ItemCondition condition,
            @RequestParam(required = false) Boolean offerAccepted,
            @RequestParam(required = false, defaultValue = "false") boolean onlyWish,
            Authentication authentication,
            @PageableDefault(size = 6, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable,
            Model model
    ) {
        Long loginMemberId = null;

        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {

            String username = authentication.getName();

            Member member =
                    memberRepository.findByUsername(username)
                            .orElse(null);

            if (member != null) {
                loginMemberId = member.getId();
            }
        }

        model.addAttribute("memberId", loginMemberId);

        // 찜 목록
        if (onlyWish) {

            if (authentication == null) {
                return "redirect:/member/login";
            }

            String username = authentication.getName();

            Member member =
                    memberRepository.findByUsername(username)
                            .orElse(null);

            if (member == null) {
                return "redirect:/member/login";
            }

            Page<UsedPost> result =
                    usedPostService.getWishPosts(
                            member.getId(),
                            pageable
                    );

            Page<UsedPostDTO> posts =
                    result.map(UsedPostDTO::new);

            model.addAttribute("posts", posts);
            model.addAttribute("onlyWish", true);

            model.addAttribute("keyword", keyword);
            model.addAttribute("category", category);
            model.addAttribute("region", region);
            model.addAttribute("condition", condition);
            model.addAttribute("offerAccepted", offerAccepted);

            addPagingAttributes(model, result, pageable);

            return "used/list";
        }

        // 검색 조건 정리
        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }

        if (category != null && category.isBlank()) {
            category = null;
        }

        if (region != null && region.isBlank()) {
            region = null;
        }

        if (offerAccepted != null && !offerAccepted) {
            offerAccepted = null;
        }

        Page<UsedPostDTO> posts =
                usedPostService.searchListDto(
                        keyword,
                        category,
                        region,
                        condition,
                        offerAccepted,
                        pageable
                );

        model.addAttribute("posts", posts);

        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("region", region);
        model.addAttribute("condition", condition);
        model.addAttribute("offerAccepted", offerAccepted);
        model.addAttribute("onlyWish", onlyWish);

        addPagingAttributes(model, posts, pageable);

        return "used/list";
    }

    // 게시글 작성 페이지
    @GetMapping("/used/write")
    public String writeForm(
            Authentication authentication,
            Model model
    ) {

        if (authentication == null) {
            return "redirect:/member/login";
        }

        String username = authentication.getName();

        Member member =
                memberRepository.findByUsername(username)
                        .orElse(null);

        if (member != null) {

            String writerName =
                    member.getNickname() != null
                            && !member.getNickname().isBlank()
                            ? member.getNickname()
                            : member.getUsername();

            model.addAttribute("writerName", writerName);
            model.addAttribute("memberId", member.getId());
        }

        return "used/write";
    }

    // 게시글 작성
    @PostMapping("/used/write")
    public String write(
            @ModelAttribute UsedPostDTO dto,
            @RequestParam(value = "images", required = false)
            List<MultipartFile> files,
            Principal principal
    ) throws IOException {

        if (principal == null) {
            return "redirect:/member/login";
        }

        String username = principal.getName();

        usedPostService.write(dto, files, username);

        return "redirect:/used/list";
    }

    // 게시글 상세 조회
    @GetMapping("/used/detail/{id}")
    public String detail(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {

        UsedPostDTO post =
                usedPostService.getDetail(id);

        if (post == null
                || Boolean.TRUE.equals(post.getIsHidden())) {

            return "redirect:/used/list";
        }

        Long memberId = null;

        if (authentication != null) {

            String username = authentication.getName();

            Member member =
                    memberRepository.findByUsername(username)
                            .orElse(null);

            if (member != null) {
                memberId = member.getId();
            }
        }

        boolean isWriter =
                memberId != null
                        && memberId.equals(post.getMemberId());

        boolean wished =
                memberId != null
                        && wishlistService.isWished(memberId, id);

        boolean alreadyReported = false;

        if (memberId != null) {
            alreadyReported =
                    usedPostReportRepository
                            .existsByUsedPost_IdAndMember_Id(
                                    id,
                                    memberId
                            );
        }

        Double mannerAverage =
                post.getMannerAverage() != null
                        ? post.getMannerAverage()
                        : 0.0;

        model.addAttribute("mannerAverage", mannerAverage);
        model.addAttribute("alreadyReported", alreadyReported);

        model.addAttribute("post", post);
        model.addAttribute("isWriter", isWriter);
        model.addAttribute("wished", wished);
        model.addAttribute("memberId", memberId);

        return "used/detail";
    }

    // 게시글 신고
    @PostMapping("/used/report/{id}")
    public String report(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam(required = false) String content,
            Authentication authentication
    ) {

        if (authentication == null) {
            return "redirect:/member/login";
        }

        String username = authentication.getName();

        Member member =
                memberRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException("회원 없음"));

        try {

            usedPostService.reportPost(
                    id,
                    member.getId(),
                    reason,
                    content
            );

        } catch (RuntimeException e) {

            return "redirect:/used/detail/" + id
                    + "?reportError=true";
        }

        return "redirect:/used/detail/" + id
                + "?reportSuccess=true";
    }

    // 게시글 수정 페이지
    @GetMapping("/used/edit/{id}")
    public String editForm(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {

        UsedPostDTO post =
                usedPostService.getDetail(id);

        if (post == null
                || Boolean.TRUE.equals(post.getIsHidden())) {

            return "redirect:/used/list";
        }

        if (authentication == null) {
            return "redirect:/member/login";
        }

        String username = authentication.getName();

        Member member =
                memberRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException("회원 없음"));

        if (!member.getId().equals(post.getMemberId())) {
            return "redirect:/used/detail/" + id;
        }

        model.addAttribute("post", post);

        return "used/edit";
    }

    // 게시글 수정
    @PostMapping("/used/edit/{id}")
    public String edit(
            @PathVariable Long id,
            @ModelAttribute UsedPostDTO dto,
            Authentication authentication
    ) {

        UsedPostDTO post =
                usedPostService.getDetail(id);

        if (authentication == null || post == null) {
            return "redirect:/member/login";
        }

        String username = authentication.getName();

        Member member =
                memberRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException("회원 없음"));

        if (!member.getId().equals(post.getMemberId())) {
            return "redirect:/used/detail/" + id;
        }

        usedPostService.edit(id, dto);

        return "redirect:/used/detail/" + id;
    }

    // 판매 상태 변경
    @PostMapping("/used/status/{id}")
    public String changeStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication
    ) {

        UsedPostDTO post =
                usedPostService.getDetail(id);

        if (authentication == null || post == null) {
            return "redirect:/member/login";
        }

        String username = authentication.getName();

        Member member =
                memberRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException("회원 없음"));

        if (!member.getId().equals(post.getMemberId())) {
            return "redirect:/used/detail/" + id;
        }

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

    // 판매 완료 처리
    @PostMapping("/used/complete/{id}")
    public String completeSale(
            @PathVariable Long id,
            Authentication authentication
    ) {

        UsedPostDTO post =
                usedPostService.getDetail(id);

        if (authentication == null || post == null) {
            return "redirect:/member/login";
        }

        String username = authentication.getName();

        Member member =
                memberRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException("회원 없음"));

        if (!member.getId().equals(post.getMemberId())) {
            return "redirect:/used/detail/" + id;
        }

        usedPostService.completeSale(id);

        return "redirect:/used/detail/" + id;
    }

    // 찜 목록 조회
    @GetMapping("/wishlist/list")
    public String wishList(
            Authentication authentication,
            Model model
    ) {

        if (authentication == null) {
            return "redirect:/member/login";
        }

        String username = authentication.getName();

        Member member =
                memberRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException("회원 없음"));

        Long memberId = member.getId();

        List<Long> wishIds =
                wishlistService.getWishPostIds(memberId);

        List<UsedPostDTO> wishPosts =
                usedPostService.getWishList(wishIds);

        model.addAttribute("posts", wishPosts);

        return "wishlist/list";
    }

    // 게시글 삭제
    @PostMapping("/used/delete/{id}")
    public String deletePost(
            @PathVariable Long id,
            Authentication authentication
    ) {

        if (authentication == null) {
            return "redirect:/member/login";
        }

        UsedPost post =
                usedPostRepository.findById(id)
                        .orElse(null);

        if (post == null) {
            return "redirect:/used/list";
        }

        String username = authentication.getName();

        Member member =
                memberRepository.findByUsername(username)
                        .orElse(null);

        if (member == null) {
            return "redirect:/member/login";
        }

        if (!post.getMember().getId().equals(member.getId())) {
            return "redirect:/used/detail/" + id;
        }

        post.setIsHidden(true);
        usedPostRepository.save(post);

        return "redirect:/used/list";
    }
}