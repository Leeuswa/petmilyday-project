package com.petmilyday.dto.community;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetupCommentDTO {
    private Long id;

    @NotNull
    private Long meetupPostId;

    @NotEmpty
    private String content;

    private String writerName;
    private String writerUsername;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private Long parentId;
    private int likeCount;
    private boolean likedByCurrentUser;
}