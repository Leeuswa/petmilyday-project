package com.petmilyday.dto.review;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalReviewRequestDTO {

    private Long hospitalId;      // 어느 병원에 리뷰 쓸지
    private Long reservationId;   // 어떤 예약 건인지
    private Integer rating;       // 별점 1~5
    private String content;       // 리뷰 내용
}