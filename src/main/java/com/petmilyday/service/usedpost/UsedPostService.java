package com.petmilyday.service.usedpost;

import com.petmilyday.entity.used.ItemCondition;
import com.petmilyday.entity.used.UsedPost;
import com.petmilyday.entity.used.UsedPostStatus;
import com.petmilyday.dto.usedpost.UsedPostDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UsedPostService {

    Page<UsedPostDTO> getList(Pageable pageable);

    UsedPostDTO getDetail(Long id);

    void write(
            UsedPostDTO dto,
            List<MultipartFile> files,
            String username
    ) throws IOException;

    // 검색 + 필터
    Page<UsedPost> searchList(
            String keyword,
            String category,
            String region,
            ItemCondition condition,
            Boolean offerAccepted,
            Pageable pageable
    );

    void reportPost(
            Long postId,
            Long memberId,
            String reason,
            String content
    );

    void update(Long id, UsedPostDTO dto);

    void edit(Long id, UsedPostDTO dto);

    void changeStatus(Long id, UsedPostStatus status);

    void completeSale(Long postId);

    List<UsedPostDTO> getWishList(List<Long> ids);

    Page<UsedPost> getWishPosts(
            Long memberId,
            Pageable pageable
    );


}