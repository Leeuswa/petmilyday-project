package com.petmilyday.service.admin;

import com.petmilyday.dto.admin.AdminReservationDTO;
import com.petmilyday.dto.admin.ReservationSearchDTO;
import org.springframework.data.domain.Page;

public interface AdminReservationService {

    // 예약 리스트 검색/필터 + 페이징
    Page<AdminReservationDTO> reservationList(ReservationSearchDTO searchDTO, int page);

    // 예약 상세보기
    AdminReservationDTO reservationDetail(Long reservationId);

    // 예약 승인
    void approveReservation(Long reservationId);

    // 예약 취소
    void cancelReservation(Long reservationId);
}