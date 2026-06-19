package com.petmilyday.controller.community;

import com.petmilyday.dto.community.PageRequestDTO;
import com.petmilyday.dto.community.PageResponseDTO;
import com.petmilyday.dto.community.CommentDTO;
import com.petmilyday.service.community.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// [필수] 팀 컨벤션에 맞춘 REST 컨트롤러
@RestController
@RequestMapping("/comments")
@Log4j2
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> register(@Valid @RequestBody CommentDTO commentDTO) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Long id = commentService.register(username, commentDTO);

        Map<String, Long> resultMap = new HashMap<>();
        resultMap.put("id", id);
        return resultMap;
    }

    @GetMapping(value = "/list/{postId}")
    public PageResponseDTO<CommentDTO> getList(@PathVariable("postId") Long postId,
                                               PageRequestDTO pageRequestDTO) {
        return commentService.getListOfPost(postId, pageRequestDTO);
    }

    @GetMapping(value = "/{id}")
    public CommentDTO getCommentDTO(@PathVariable("id") Long id) {
        return commentService.read(id);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> modify(@PathVariable("id") Long id,
                                    @RequestBody CommentDTO commentDTO) {
        commentDTO.setId(id);
        commentService.modify(commentDTO);

        Map<String, Long> resultMap = new HashMap<>();
        resultMap.put("id", id);
        return resultMap;
    }

    // [기능] DELETE 방식으로 댓글 삭제
    @DeleteMapping("/{id}")
    public Map<String, Long> remove(@PathVariable("id") Long id) {
        commentService.remove(id);

        Map<String, Long> resultMap = new HashMap<>();
        resultMap.put("id", id);
        return resultMap;
    }

    @PostMapping("/{id}/like")
    public java.util.Map<String, Object> toggleLike(@PathVariable("id") Long id) {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // 좋아요 토글 실행 후 최신 개수 획득
        int latestLikeCount = commentService.toggleLike(username, id);

        java.util.Map<String, Object> resultMap = new java.util.HashMap<>();
        resultMap.put("likeCount", latestLikeCount);
        return resultMap;
    }
}