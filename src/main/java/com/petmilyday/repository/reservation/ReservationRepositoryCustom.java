package com.petmilyday.repository.reservation;

import com.petmilyday.dto.admin.ReservationSearchDTO;
import com.petmilyday.entity.reservation.Reservation;
import com.petmilyday.entity.reservation.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ReservationRepositoryCustom {

    // 슬롯 중복 체크 (취소 제외한 예약 수)
    long countAvailableSlot(Long hospitalId, LocalDate reserveDate, LocalTime reserveTime);

    // 메인 어드민 - 전체 예약 검색/필터 + 페이징
    Page<Reservation> searchAdminReservationsPage(ReservationSearchDTO searchDTO, Pageable pageable);

    // 병원 어드민 - 담당 병원 예약 상태/날짜 필터 + 페이징
    Page<Reservation> searchHospitalReservationsPage(
            Long hospitalId,
            ReservationStatus status,
            LocalDate dateFrom,
            LocalDate dateTo,
            Pageable pageable
    );
}