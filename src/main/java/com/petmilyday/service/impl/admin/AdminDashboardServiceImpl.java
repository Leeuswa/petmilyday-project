package com.petmilyday.service.impl.admin;

import com.petmilyday.dto.admin.AdminDashboardDTO;
import com.petmilyday.entity.hospital.HospitalManagerStatus;
import com.petmilyday.entity.reservation.Reservation;
import com.petmilyday.entity.reservation.ReservationStatus;
import com.petmilyday.repository.hospital.HospitalManagerRepository;
import com.petmilyday.repository.hospital.HospitalRepository;
import com.petmilyday.repository.reservation.ReservationRepository;
import com.petmilyday.service.admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final HospitalRepository hospitalRepository;
    private final HospitalManagerRepository hospitalManagerRepository;
    private final ReservationRepository reservationRepository;

    // 관리자 대시보드 통계 조회
    @Override
    public AdminDashboardDTO getDashboard() {

        // 전체 예약 목록 조회
        List<Reservation> reservationList = reservationRepository.findAll();

        // 승인 대기 예약 수
        long waitingReservationCount = reservationList.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.WAITING)
                .count();

        // 승인 완료 예약 수
        long approvedReservationCount = reservationList.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.APPROVED)
                .count();

        // 취소 예약 수
        long cancelReservationCount = reservationList.stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.CANCEL)
                .count();

        // 승인 대기 중인 병원 관리자 신청 수
        long waitingHospitalManagerCount =
                hospitalManagerRepository.findByStatus(HospitalManagerStatus.WAITING).size();

        // 대시보드에 표시할 통계 데이터 반환
        return AdminDashboardDTO.builder()
                .hospitalCount(hospitalRepository.count())
                .waitingHospitalManagerCount(waitingHospitalManagerCount)
                .reservationCount(reservationList.size())
                .waitingReservationCount(waitingReservationCount)
                .approvedReservationCount(approvedReservationCount)
                .cancelReservationCount(cancelReservationCount)
                .build();
    }
}