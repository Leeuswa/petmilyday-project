package com.petmilyday.service.impl.admin;

import com.petmilyday.dto.admin.AdminReservationDTO;
import com.petmilyday.entity.reservation.Reservation;
import com.petmilyday.repository.reservation.ReservationRepository;
import com.petmilyday.service.admin.AdminReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminReservationServiceImpl implements AdminReservationService {

    private final ReservationRepository reservationRepository;

    // 예약 리스트 페이징
    @Override
    @Transactional(readOnly = true)
    public Page<AdminReservationDTO> reservationList(int page) {

        Pageable pageable = PageRequest.of(page, 10);

        return reservationRepository.findAllForAdmin(pageable)
                .map(this::toDTO);
    }

    // 예약 상세보기
    @Override
    @Transactional(readOnly = true)
    public AdminReservationDTO reservationDetail(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약 정보가 없습니다."));

        return toDTO(reservation);
    }

    @Override
    public void approveReservation(Long reservationId) {
        Reservation reservation = reservationRepository
                .findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약 정보가 없습니다."));

        reservation.approve();
    }

    @Override
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약 정보가 없습니다."));

        reservation.adminCancel();
    }

    private AdminReservationDTO toDTO(Reservation reservation) {
        return AdminReservationDTO.builder()
                .id(reservation.getId())
                .hospitalName(reservation.getHospital().getName())
                .memberName(reservation.getMember().getName())
                .reservationDate(reservation.getReserveDate())
                .reservationTime(reservation.getReserveTime())
                .status(reservation.getStatus())
                .build();
    }
}