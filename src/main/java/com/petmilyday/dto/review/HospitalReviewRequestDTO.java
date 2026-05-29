package com.petmilyday.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalReviewRequestDTO {

    @NotNull(message = "병원 정보가 없습니다.")
    private Long hospitalId;      // 어느 병원에 리뷰 쓸지
    private Long reservationId;   // 어떤 예약 건인지

    @NotNull(message = "평점을 선택해 주세요")
    @Min(value = 1,message = "평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5점 이하야 합니다.")
    private Integer rating;

    @NotBlank(message = "리뷰 내용은 필수 입니다.")
    private String content;
}