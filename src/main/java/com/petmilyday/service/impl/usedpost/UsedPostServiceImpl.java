package com.petmilyday.service.impl.usedpost;

import com.petmilyday.dto.usedpost.ChatRoomListDTO;
import com.petmilyday.dto.usedpost.UsedPostDTO;
import com.petmilyday.entity.chat.ChatRoom;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.used.*;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.used.MannerScoreRepository;
import com.petmilyday.repository.used.UsedPostImgRepository;
import com.petmilyday.repository.used.UsedPostReportRepository;
import com.petmilyday.repository.used.UsedPostRepository;
import com.petmilyday.repository.wishlist.WishlistRepository;
import com.petmilyday.service.product.S3UploadService;
import com.petmilyday.service.usedpost.UsedPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsedPostServiceImpl implements UsedPostService {

    private final UsedPostRepository usedPostRepository;
    private final UsedPostImgRepository imgRepository;
    private final WishlistRepository wishlistRepository;
    private final MemberRepository memberRepository;
    private final UsedPostReportRepository usedPostReportRepository;
    private final S3UploadService s3UploadService;
    private final MannerScoreRepository mannerScoreRepository;

    // 전체 목록
    @Override
    public Page<UsedPostDTO> getList(Pageable pageable) {

        return usedPostRepository.findAll(pageable)
                .map(UsedPostDTO::new);
    }

    @Override
    @Transactional(readOnly = true)
    public UsedPostDTO getDetail(Long id) {

        UsedPost post = usedPostRepository.findDetail(id);

        if (post == null) {
            return null;
        }

        UsedPostDTO dto = new UsedPostDTO(post);

        if (post.getMember() != null) {
            Double average =
                    mannerScoreRepository.findAverageScoreByMemberId(
                            post.getMember().getId()
                    );

            dto.setMannerAverage(average);
        } else {
            dto.setMannerAverage(0.0);
        }

        return dto;
    }

    // 검색
    @Override
    public Page<UsedPost> searchList(
            String keyword,
            String category,
            String region,
            ItemCondition condition,
            Boolean offerAccepted,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable
    ) {

        return usedPostRepository.searchList(
                keyword,
                category,
                region,
                condition,
                offerAccepted,
                minPrice,
                maxPrice,
                LocalDateTime.now().minusHours(24),
                pageable
        );
    }

    // 글 작성
    @Override
    @Transactional
    public void write(
            UsedPostDTO dto,
            List<MultipartFile> files,
            String username
    ) throws IOException {

        if (username == null || username.isBlank()) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        UsedPost post = dto.toEntity();

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("회원 없음"));

        post.setMember(member);

        post.setOfferAccepted(
                Boolean.TRUE.equals(dto.getOfferAccepted())
        );

        post.setCreatedAt(LocalDateTime.now());
        post.setIsHidden(false);

        UsedPost savedPost =
                usedPostRepository.save(post);

        // 이미지 S3 저장
        if (files != null && !files.isEmpty()) {

            for (MultipartFile file : files) {

                if (file.isEmpty()) {
                    continue;
                }

                String imageUrl =
                        s3UploadService.uploadFile(file);

                UsedPostImg img =
                        UsedPostImg.builder()
                                .imgUrl(imageUrl)
                                .usedPost(savedPost)
                                .build();

                imgRepository.save(img);
            }
        }
    }

    // 신고
    @Override
    @Transactional
    public void reportPost(
            Long postId,
            Long memberId,
            String reason,
            String content
    ) {

        UsedPost post =
                usedPostRepository.findById(postId)
                        .orElseThrow(() ->
                                new RuntimeException("게시글 없음"));

        Member member =
                memberRepository.findById(memberId)
                        .orElseThrow(() ->
                                new RuntimeException("회원 없음"));

        boolean alreadyReported =
                usedPostReportRepository.existsByUsedPost_IdAndMember_Id(
                        postId,
                        memberId
                );

        if (alreadyReported) {
            throw new RuntimeException("이미 신고한 게시글입니다.");
        }

        UsedPostReport report =
                UsedPostReport.builder()
                        .usedPost(post)
                        .member(member)
                        .reason(reason)
                        .content(content)
                        .createdAt(LocalDateTime.now())
                        .build();

        usedPostReportRepository.save(report);

        post.setReportCount(post.getReportCount() + 1);

        if (post.getReportCount() >= 5) {
            post.setIsHidden(true);
        }

        usedPostRepository.save(post);
    }

    // 수정(update)
    @Override
    @Transactional
    public void update(
            Long id,
            UsedPostDTO dto
    ) {

        UsedPost post =
                usedPostRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("게시글 없음"));

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setPrice(dto.getPrice());
        post.setRegion(dto.getRegion());
        post.setCategory(dto.getCategory());
        post.setItemCondition(dto.getItemCondition());

        post.setOfferAccepted(
                Boolean.TRUE.equals(dto.getOfferAccepted())
        );

        post.setUpdatedAt(LocalDateTime.now());

        usedPostRepository.save(post);
    }

    // 수정(edit)
    @Override
    @Transactional
    public void edit(
            Long id,
            UsedPostDTO dto,
            List<MultipartFile> files
    ) throws IOException {

        UsedPost post =
                usedPostRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("게시글 없음"));

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setPrice(dto.getPrice());
        post.setRegion(dto.getRegion());
        post.setCategory(dto.getCategory());
        post.setItemCondition(dto.getItemCondition());

        post.setOfferAccepted(
                Boolean.TRUE.equals(dto.getOfferAccepted())
        );

        post.setUpdatedAt(LocalDateTime.now());

        boolean hasNewImage =
                files != null
                        && files.stream().anyMatch(file -> file != null && !file.isEmpty());

        if (hasNewImage) {

            imgRepository.deleteByUsedPost_Id(post.getId());

            for (MultipartFile file : files) {

                if (file == null || file.isEmpty()) {
                    continue;
                }

                String imageUrl =
                        s3UploadService.uploadFile(file);

                UsedPostImg img =
                        UsedPostImg.builder()
                                .imgUrl(imageUrl)
                                .usedPost(post)
                                .build();

                imgRepository.save(img);
            }
        }

        usedPostRepository.save(post);
    }

    // 상태 변경
    @Override
    @Transactional
    public void changeStatus(
            Long id,
            UsedPostStatus status
    ) {

        UsedPost post =
                usedPostRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("게시글 없음"));

        post.setStatus(status);

        usedPostRepository.save(post);
    }

    // 판매 완료
    @Override
    @Transactional
    public void completeSale(Long postId) {

        usedPostRepository.updateStatus(
                postId,
                UsedPostStatus.SOLD
        );
    }

    // 찜 목록 DTO
    @Override
    @Transactional(readOnly = true)
    public List<UsedPostDTO> getWishList(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return usedPostRepository.findWishPostsForDto(ids)
                .stream()
                .map(UsedPostDTO::new)
                .toList();
    }

    // 찜 목록 페이징
    @Override
    public Page<UsedPost> getWishPosts(
            Long memberId,
            Pageable pageable
    ) {

        List<Long> ids =
                wishlistRepository.findUsedPostIdsByMemberId(memberId);

        if (ids.isEmpty()) {
            return Page.empty(pageable);
        }

        return usedPostRepository.findWishPosts(
                ids,
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsedPostDTO> searchListDto(
            String keyword,
            String category,
            String region,
            ItemCondition condition,
            Boolean offerAccepted,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable
    ) {

        return usedPostRepository.searchList(
                        keyword,
                        category,
                        region,
                        condition,
                        offerAccepted,
                        minPrice,
                        maxPrice,
                        LocalDateTime.now().minusHours(24),
                        pageable
                )
                .map(post -> {
                    UsedPostDTO dto = new UsedPostDTO(post);

                    if (post.getMember() != null) {
                        Double average =
                                mannerScoreRepository.findAverageScoreByMemberId(
                                        post.getMember().getId()
                                );

                        dto.setMannerAverage(average);
                    } else {
                        dto.setMannerAverage(0.0);
                    }

                    return dto;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsedPostDTO> getWishPostsDto(
            Long memberId,
            Pageable pageable
    ) {

        List<Long> ids =
                wishlistRepository.findUsedPostIdsByMemberId(memberId);

        if (ids.isEmpty()) {
            return Page.empty(pageable);
        }

        return usedPostRepository.findWishPosts(
                        ids,
                        pageable
                )
                .map(UsedPostDTO::new);
    }

    @Override
    @Transactional
    public void pullUp(Long postId, Long memberId) {

        UsedPost post =
                usedPostRepository.findById(postId)
                        .orElseThrow(() ->
                                new RuntimeException("게시글 없음"));

        if (post.getMember() == null
                || !post.getMember().getId().equals(memberId)) {
            throw new RuntimeException("작성자만 끌어올리기할 수 있습니다.");
        }

        if (post.getStatus() != UsedPostStatus.SELLING) {
            throw new RuntimeException("판매중 게시글만 끌어올리기할 수 있습니다.");
        }

        post.setPulledUpAt(LocalDateTime.now());

        usedPostRepository.save(post);
    }
}