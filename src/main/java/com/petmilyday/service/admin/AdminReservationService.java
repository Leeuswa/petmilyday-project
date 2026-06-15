package com.petmilyday.service.admin;

import com.petmilyday.dto.admin.AdminReservationDTO;

import java.util.List;

public interface AdminReservationService {

    // 예약 리스트
    List<AdminReservationDTO> reservationList();

    // 예약 상세보기
    AdminReservationDTO reservationDetail(Long reservationId);

    // 예약 승인
    void approveReservation(Long reservationId);

    // 예약 취소
    void cancelReservation(Long reservationId);
}
