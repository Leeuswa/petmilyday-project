package com.petmilyday.service.hospital;

import com.petmilyday.dto.hospital.HospitalRequestDTO;
import com.petmilyday.dto.review.HospitalReviewRequestDTO;
import com.petmilyday.dto.review.HospitalReviewResponseDTO;

import java.util.List;

public interface HospitalReviewService {

    //리뷰 작성
    void reviewRegister(HospitalReviewRequestDTO dto);
    //리뷰 목록 조회
    List<HospitalReviewResponseDTO> reviewList(Long hospitalId);

    //리뷰 수정
    void reviewModify(Long reviewId, HospitalReviewRequestDTO dto);
    //리뷰 삭제
    void reviewRemove(Long reviewId);





}
