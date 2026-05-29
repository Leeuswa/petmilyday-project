package com.petmilyday.service.reservation;

import com.petmilyday.dto.reservation.ReservationRequestDTO;
import com.petmilyday.dto.reservation.ReservationResponseDTO;
import com.petmilyday.dto.reservation.ReservationSlotDto;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {
    //예약신청
    void reservationRegister(ReservationRequestDTO dto);
    // 내 예약 목록 조회
    List<ReservationResponseDTO> reservationList(Long memberId);

    // 예약 취소
    void reservationCancel(Long reservationId, String cancelReason);

    List<ReservationSlotDto> getAvailableSlots(
            Long hospitalId,
            LocalDate date );

}
