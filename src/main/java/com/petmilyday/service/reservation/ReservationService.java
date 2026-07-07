package com.petmilyday.service.reservation;

import com.petmilyday.dto.reservation.ReservationRequestDTO;
import com.petmilyday.dto.reservation.ReservationResponseDTO;
import com.petmilyday.dto.reservation.ReservationSlotDto;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

    // 예약신청
    void reservationRegister(ReservationRequestDTO dto, String loginId);

    // 내 예약 목록 조회
    // jwt 정보를 가져올때 username = 로그인아이디를 가져오기위해 매개변수로 username을 받음
    List<ReservationResponseDTO> reservationList(String username);

    // 내 예약 목록 조회 + 페이징
    Page<ReservationResponseDTO> reservationListPage(String username, int page);

    // 예약 취소
    void reservationCancel(Long reservationId, String cancelReason, String username);

    List<ReservationSlotDto> getAvailableSlots(
            Long hospitalId,
            LocalDate date
    );

    // 내 동물 진료기록 조회
    List<ReservationResponseDTO> myMedicalRecords(String username);

    // 내 동물 진료기록 조회 + 페이징
    Page<ReservationResponseDTO> myMedicalRecordsPage(String username, int page);
}