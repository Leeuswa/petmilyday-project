package com.petmilyday.service.hospital;

import com.petmilyday.dto.review.HospitalReviewRequestDTO;
import com.petmilyday.dto.review.HospitalReviewResponseDTO;

import java.util.List;

public interface HospitalReviewService {

    //리뷰 작성
    void reviewRegister(HospitalReviewRequestDTO dto);
    //리뷰 목록 조회
    List<HospitalReviewResponseDTO> reviewList(Long hospitalId);
}
