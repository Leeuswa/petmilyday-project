package com.petmilyday.dto.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommunityPostDTO {

    private Long id;

    @NotBlank(message = "제목을 입력해 주세요.")
    @Size(min = 2, max = 200, message = "제목은 2자 이상 200자 이하로 작성해 주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해 주세요.")
    private String content;

    private String writerName;

    private int viewCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}