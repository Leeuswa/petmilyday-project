package com.petmilyday.service.hospital;

import com.petmilyday.dto.reservation.ReservationResponseDTO;
import com.petmilyday.entity.reservation.ReservationStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface HospitalAdminReservationService {

    // 병원 관리자 예약 목록 상태/날짜 필터 + 페이징
    Page<ReservationResponseDTO> reservationList(
            String username,
            ReservationStatus status,
            LocalDate dateFrom,
            LocalDate dateTo,
            int page
    );

    //병원 관리자가 예약 승인
    void approveReservation(Long reservationId, String username);

    //병원 관리자가 예약 거절
    void rejectReservation(Long reservationId, String username);
}