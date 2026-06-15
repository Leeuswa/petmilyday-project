package com.petmilyday.service.hospital;

import com.petmilyday.dto.reservation.ReservationResponseDTO;

import java.util.List;

public interface HospitalAdminReservationService {
    List<ReservationResponseDTO> reservationList(String username);

    //병원 관리자가 예약 승인
    void approveReservation(Long reservationId);
    //병원 관리자가 예약 거절
    void rejectReservation(Long reservationId);
}
