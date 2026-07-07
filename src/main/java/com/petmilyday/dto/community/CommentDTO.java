package com.petmilyday.dto.community;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
    private Long id;

    @NotNull
    private Long postId;

    @NotEmpty
    private String content;

    private String writerName; // 화면 표시용

    private String writerUsername; // 권한 체크용

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonIgnore
    private LocalDateTime updatedAt;

    private Long parentId; // 대댓글의 부모 댓글 확인용

    private int likeCount;

    private boolean likedByCurrentUser; // 좋아요 눌려 있는지 확인
}