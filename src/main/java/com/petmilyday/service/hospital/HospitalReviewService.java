package com.petmilyday.service.hospital;

import com.petmilyday.dto.hospital.HospitalRequestDTO;
import com.petmilyday.dto.reservation.ReservationResponseDTO;
import com.petmilyday.dto.review.HospitalReviewRequestDTO;
import com.petmilyday.dto.review.HospitalReviewResponseDTO;

import java.util.List;

public interface HospitalReviewService {

    //리뷰 작성
    void reviewRegister(HospitalReviewRequestDTO dto,String username);
    //리뷰 목록 조회
    List<HospitalReviewResponseDTO> reviewList(Long hospitalId);

    //리뷰 수정
    void reviewModify(Long reviewId, HospitalReviewRequestDTO dto,String username);
    //리뷰 삭제
    void reviewRemove(Long reviewId,String username);

    // 리뷰 작성 가능한 예약 ID 조회
    Long findReviewableReservationId(Long hospitalId, String username);

    //내가 작성한 리뷰 목록
    List<HospitalReviewResponseDTO> myReivewList(String username);




}
