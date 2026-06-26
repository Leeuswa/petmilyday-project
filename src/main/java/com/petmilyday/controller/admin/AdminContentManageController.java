package com.petmilyday.controller.admin;

import com.petmilyday.dto.product.ProductReviewAdminDto;
import com.petmilyday.entity.hospital.HospitalReview;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.product.Product;
import com.petmilyday.entity.product.ProductQna;
import com.petmilyday.entity.product.ProductReview;
import com.petmilyday.entity.used.UsedPost;
import com.petmilyday.entity.used.UsedPostReport;
import com.petmilyday.repository.hospital.HospitalReviewRepository;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.product.ProductRepository;
import com.petmilyday.repository.product.ProductReviewRepository;
import com.petmilyday.repository.shop.ProductQnaRepository;
import com.petmilyday.repository.used.UsedPostReportRepository;
import com.petmilyday.repository.used.UsedPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminContentManageController {

    private final UsedPostReportRepository usedPostReportRepository;
    private final UsedPostRepository usedPostRepository;
    private final HospitalReviewRepository hospitalReviewRepository;
    private final ProductQnaRepository productQnaRepository;
    private final ProductReviewRepository productReviewRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    // 중고마켓 게시글 신고 목록 (처리상태/키워드 검색)
    @GetMapping("/reports/used-posts")
    public String usedPostReportList(@RequestParam(required = false) Boolean hidden,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(defaultValue = "0") int page,
                                     Model model) {

        Pageable pageable = PageRequest.of(page, 10);

        Page<UsedPostReport> reportPage =
                usedPostReportRepository.searchForAdmin(hidden, keyword, pageable);

        model.addAttribute("reportPage", reportPage);
        model.addAttribute("reportList", reportPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reportPage.getTotalPages());
        model.addAttribute("hidden", hidden);
        model.addAttribute("keyword", keyword);

        return "admin/report/usedPostReportList";
    }

    // 신고 게시글 숨김 처리
    @PostMapping("/reports/used-posts/{postId}/hide")
    @Transactional
    public String hideUsedPost(@PathVariable Long postId,
                               RedirectAttributes redirectAttributes) {

        UsedPost post = usedPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        post.setIsHidden(true);

        redirectAttributes.addFlashAttribute("message", "게시글을 숨김 처리했습니다.");
        return "redirect:/admin/reports/used-posts";
    }

    // 신고 게시글 숨김 해제
    @PostMapping("/reports/used-posts/{postId}/show")
    @Transactional
    public String showUsedPost(@PathVariable Long postId,
                               RedirectAttributes redirectAttributes) {

        UsedPost post = usedPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        post.setIsHidden(false);

        redirectAttributes.addFlashAttribute("message", "게시글 숨김을 해제했습니다.");
        return "redirect:/admin/reports/used-posts";
    }

    // 병원 리뷰 신고 목록 (키워드 검색)
    @GetMapping("/reviews/hospital")
    public String hospitalReviewReportList(@RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "0") int page,
                                           Model model) {

        Pageable pageable = PageRequest.of(page, 10);

        Page<HospitalReview> reviewPage =
                hospitalReviewRepository.searchReportedReviewsForAdmin(keyword, pageable);

        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("reviewList", reviewPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "admin/review/hospitalReviewReportList";
    }

    // 병원 리뷰 신고 해제
    @PostMapping("/reviews/hospital/{reviewId}/restore")
    @Transactional
    public String restoreHospitalReview(@PathVariable Long reviewId,
                                        RedirectAttributes redirectAttributes) {

        HospitalReview review = hospitalReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        review.restoreReport();

        redirectAttributes.addFlashAttribute("message", "리뷰 신고를 해제했습니다.");
        return "redirect:/admin/reviews/hospital";
    }

    // 병원 리뷰 삭제
    @PostMapping("/reviews/hospital/{reviewId}/delete")
    @Transactional
    public String deleteHospitalReview(@PathVariable Long reviewId,
                                       RedirectAttributes redirectAttributes) {

        hospitalReviewRepository.deleteById(reviewId);

        redirectAttributes.addFlashAttribute("message", "리뷰를 삭제했습니다.");
        return "redirect:/admin/reviews/hospital";
    }

    // QnA 관리 목록 (상태/키워드 검색)
    @GetMapping("/qna")
    public String qnaList(@RequestParam(required = false) String status,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {

        Pageable pageable = PageRequest.of(page, 10);

        Page<ProductQna> qnaPage =
                productQnaRepository.searchForAdmin(status, keyword, pageable);

        model.addAttribute("qnaPage", qnaPage);
        model.addAttribute("qnaList", qnaPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", qnaPage.getTotalPages());
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);

        return "admin/qna/qnaList";
    }

    // QnA 답변 등록
    @PostMapping("/qna/{qnaId}/answer")
    @Transactional
    public String answerQna(@PathVariable Long qnaId,
                            @RequestParam String answer,
                            RedirectAttributes redirectAttributes) {

        ProductQna qna = productQnaRepository.findById(qnaId)
                .orElseThrow(() -> new RuntimeException("문의글을 찾을 수 없습니다."));

        qna.setAnswer(answer);
        qna.setStatus("ANSWERED");

        redirectAttributes.addFlashAttribute("message", "답변을 등록했습니다.");
        return "redirect:/admin/qna";
    }

    // QnA 삭제
    @PostMapping("/qna/{qnaId}/delete")
    @Transactional
    public String deleteQna(@PathVariable Long qnaId,
                            RedirectAttributes redirectAttributes) {

        productQnaRepository.deleteById(qnaId);

        redirectAttributes.addFlashAttribute("message", "QnA를 삭제했습니다.");
        return "redirect:/admin/qna";
    }

    // 상품 리뷰 전체 조회 (키워드 검색, 신고 여부 무관)
    @GetMapping("/reviews/product")
    public String productReviewList(@RequestParam(required = false) String keyword,
                                    @RequestParam(defaultValue = "0") int page,
                                    Model model) {

        Pageable pageable = PageRequest.of(page, 10);

        Page<ProductReviewAdminDto> reviewPage = productReviewRepository
                .searchReviewsForAdmin(keyword, pageable)
                .map(review -> {
                    Product product = productRepository.findById(review.getProductId()).orElse(null);
                    Member member = memberRepository.findById(review.getMemberId()).orElse(null);

                    return ProductReviewAdminDto.builder()
                            .id(review.getId())
                            .productName(product != null ? product.getName() : "삭제된 상품")
                            .memberDisplayName(member != null ? member.getDisplayName() : "알 수 없음")
                            .rating(review.getRating())
                            .content(review.getContent())
                            .imgUrl(review.getImgUrl())
                            .createdAt(review.getCreatedAt())
                            .isReported(Boolean.TRUE.equals(review.getIsReported()))
                            .build();
                });

        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("reviewList", reviewPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "admin/review/productReviewReportList";
    }

    // 상품 리뷰 신고 해제
    @PostMapping("/reviews/product/{reviewId}/restore")
    @Transactional
    public String restoreProductReview(@PathVariable Long reviewId,
                                       RedirectAttributes redirectAttributes) {

        try {
            ProductReview review = productReviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

            review.restoreReport();

            redirectAttributes.addFlashAttribute("message", "리뷰 신고를 해제했습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/reviews/product";
    }

    // 상품 리뷰 삭제
    @PostMapping("/reviews/product/{reviewId}/delete")
    @Transactional
    public String deleteProductReview(@PathVariable Long reviewId,
                                      RedirectAttributes redirectAttributes) {

        try {
            if (!productReviewRepository.existsById(reviewId)) {
                throw new RuntimeException("리뷰를 찾을 수 없습니다.");
            }

            productReviewRepository.deleteById(reviewId);

            redirectAttributes.addFlashAttribute("message", "리뷰를 삭제했습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/reviews/product";
    }
}