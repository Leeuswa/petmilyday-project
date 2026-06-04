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
    private Long hospitalId;
    private String hospitalName;
    private Long memberId;
    private String memberNickname;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
}