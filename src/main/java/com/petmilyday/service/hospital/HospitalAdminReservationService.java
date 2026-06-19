package com.petmilyday.service.hospital;

import com.petmilyday.dto.reservation.ReservationResponseDTO;
import org.springframework.data.domain.Page;

public interface HospitalAdminReservationService {

    Page<ReservationResponseDTO> reservationList(String username, int page);

    //병원 관리자가 예약 승인
    void approveReservation(Long reservationId);

    //병원 관리자가 예약 거절
    void rejectReservation(Long reservationId);
}