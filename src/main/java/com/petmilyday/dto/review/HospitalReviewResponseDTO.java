package com.petmilyday.dto.review;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalReviewResponseDTO {

    private Long id;
    private String memberNickname;  // 작성자 닉네임
    private Integer rating;         // 별점
    private String content;         // 리뷰 내용
    private LocalDateTime createdAt; // 작성일
}