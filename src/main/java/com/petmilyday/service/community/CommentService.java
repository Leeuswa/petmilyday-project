package com.petmilyday.service.community;

import com.petmilyday.dto.community.PageRequestDTO;
import com.petmilyday.dto.community.PageResponseDTO;
import com.petmilyday.dto.community.CommentDTO;

public interface CommentService {
    Long register(String username, CommentDTO commentDTO);
    CommentDTO read(Long id);
    void modify(CommentDTO commentDTO);
    void remove(Long id);

    PageResponseDTO<CommentDTO> getListOfPost(Long postId, PageRequestDTO pageRequestDTO);

    int toggleLike(String username, Long commentId); // 토글 후 최종 좋아요 개수 반환
}