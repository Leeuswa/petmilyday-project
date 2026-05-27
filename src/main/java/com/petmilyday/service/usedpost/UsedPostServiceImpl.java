package com.petmilyday.service.usedpost;

import com.petmilyday.domain.member.Member;
import com.petmilyday.domain.member.MemberRepository;
import com.petmilyday.domain.used.*;
import com.petmilyday.domain.wishlist.WishlistRepository;
import com.petmilyday.dto.usedpost.UsedPostDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsedPostServiceImpl implements UsedPostService {

    private final UsedPostRepository usedPostRepository;
    private final UsedPostImgRepository imgRepository;
    private final WishlistRepository wishlistRepository;
    private final MemberRepository memberRepository;

    // =========================
    // 전체 목록
    // =========================
    @Override
    public Page<UsedPostDTO> getList(Pageable pageable) {

        return usedPostRepository.findAll(pageable)
                .map(UsedPostDTO::new);
    }

    // =========================
    // 상세
    // =========================
    @Override
    public UsedPostDTO getDetail(Long id) {

        UsedPost post = usedPostRepository.findDetail(id);

        if (post == null) {
            return null;
        }

        return new UsedPostDTO(post);
    }

    // =========================
    // 검색
    // =========================
    @Override
    public Page<UsedPost> searchList(
            String keyword,
            String category,
            String region,
            ItemCondition condition,
            Boolean offerAccepted,
            Pageable pageable
    ) {

        return usedPostRepository.searchList(
                keyword,
                category,
                region,
                condition,
                offerAccepted,
                pageable
        );
    }

    // =========================
    // 글 작성
    // =========================
    @Override
    @Transactional
    public void write(
            UsedPostDTO dto,
            List<MultipartFile> files,
            Long memberId
    ) throws IOException {

        if (memberId == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        UsedPost post = dto.toEntity();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new RuntimeException("회원 없음"));

        post.setMember(member);

        post.setOfferAccepted(Boolean.TRUE.equals(dto.getOfferAccepted()));

        post.setCreatedAt(LocalDateTime.now());

        UsedPost savedPost =
                usedPostRepository.save(post);

        // 이미지 저장
        if (files != null && !files.isEmpty()) {

            String uploadDir =
                    System.getProperty("user.dir")
                            + "/uploads/";

            File dir = new File(uploadDir);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            for (MultipartFile file : files) {

                if (file.isEmpty()) {
                    continue;
                }

                String uuid =
                        UUID.randomUUID().toString();

                String fileName =
                        uuid + "_"
                                + file.getOriginalFilename();

                File saveFile =
                        new File(uploadDir + fileName);

                file.transferTo(saveFile);

                UsedPostImg img = UsedPostImg.builder()
                        .imgUrl("/uploads/" + fileName)
                        .usedPost(savedPost)
                        .build();

                imgRepository.save(img);
            }
        }
    }

    // =========================
    // 신고
    // =========================
    @Override
    @Transactional
    public void reportPost(Long postId) {

        UsedPost post =
                usedPostRepository.findById(postId)
                        .orElseThrow(() ->
                                new RuntimeException("게시글 없음"));

        int currentCount =
                post.getReportCount() != null
                        ? post.getReportCount()
                        : 0;

        currentCount++;

        post.setReportCount(currentCount);

        // 신고 5회 이상 숨김
        if (currentCount >= 5) {

            post.setIsHidden(true);
        }

        usedPostRepository.save(post);
    }

    // =========================
    // 수정(update)
    // =========================
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

        post.setOfferAccepted(Boolean.TRUE.equals(dto.getOfferAccepted()));

        post.setUpdatedAt(LocalDateTime.now());

        usedPostRepository.save(post);
    }

    // =========================
    // 수정(edit)
    // =========================
    @Override
    @Transactional
    public void edit(
            Long id,
            UsedPostDTO dto
    ) {

        update(id, dto);
    }

    // =========================
    // 상태 변경
    // =========================
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

    // =========================
    // 판매 완료
    // =========================
    @Override
    @Transactional
    public void completeSale(Long postId) {

        UsedPost post =
                usedPostRepository.findById(postId)
                        .orElseThrow(() ->
                                new RuntimeException("게시글 없음"));

        post.setStatus(UsedPostStatus.SOLD);

        usedPostRepository.save(post);
    }

    // =========================
    // 찜 목록 DTO
    // =========================
    @Override
    public List<UsedPostDTO> getWishList(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return usedPostRepository.findAllById(ids)
                .stream()
                .filter(post -> post.getIsHidden() == null || !post.getIsHidden())
                .map(UsedPostDTO::new)
                .toList();
    }

    // =========================
    // 찜 목록 페이징
    // =========================
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
}