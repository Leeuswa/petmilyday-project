package com.petmilyday.service.reservation;

import com.petmilyday.dto.reservation.ReservationRequestDTO;
import com.petmilyday.dto.reservation.ReservationResponseDTO;

import java.util.List;

public interface ReservationService {
    //예약신청
    void reservationRegister(ReservationRequestDTO dto);
    // 내 예약 목록 조회
    List<ReservationResponseDTO> reservationList(Long memberId);

    // 예약 취소
    void reservationCancel(Long reservationId, String cancelReason);
}
