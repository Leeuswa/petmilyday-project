package com.petmilyday.service.impl.hospital;

import com.petmilyday.dto.notification.NotificationDTO;
import com.petmilyday.dto.reservation.ReservationResponseDTO;
import com.petmilyday.entity.hospital.HospitalManager;
import com.petmilyday.entity.hospital.HospitalManagerStatus;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.reservation.Reservation;
import com.petmilyday.entity.reservation.ReservationStatus;
import com.petmilyday.repository.hospital.HospitalManagerRepository;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.reservation.ReservationRepository;
import com.petmilyday.service.hospital.HospitalAdminReservationService;
import com.petmilyday.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class HospitalAdminReservationServiceImpl implements HospitalAdminReservationService {

    private final MemberRepository memberRepository;
    private final HospitalManagerRepository hospitalManagerRepository;
    private final ReservationRepository reservationRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    // 병원 관리자 예약 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponseDTO> reservationList(String username, int page) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원이 없습니다."));

        HospitalManager hospitalManager = hospitalManagerRepository
                .findByMemberIdAndStatus(member.getId(), HospitalManagerStatus.APPROVED)
                .orElseThrow(() -> new RuntimeException("승인된 병원 관리자가 아닙니다."));

        Long hospitalId = hospitalManager.getHospital().getId();

        Pageable pageable = PageRequest.of(page, 10);

        return reservationRepository.findHospitalReservationsPage(hospitalId, pageable)
                .map(reservation -> {
                    ReservationResponseDTO dto =
                            modelMapper.map(reservation, ReservationResponseDTO.class);

                    dto.setId(reservation.getId());
                    dto.setHospitalId(reservation.getHospital().getId());
                    dto.setHospitalName(reservation.getHospital().getName());

                    if (reservation.getPet() != null) {
                        dto.setPetName(reservation.getPet().getName());
                    }

                    dto.setReserveDate(reservation.getReserveDate());
                    dto.setReserveTime(reservation.getReserveTime());
                    dto.setDepartment(reservation.getDepartment());
                    dto.setStatus(reservation.getStatus().name());
                    dto.setWaitNumber(reservation.getWaitNumber());
                    dto.setCancelReason(reservation.getCancelReason());
                    dto.setCreatedAt(reservation.getCreatedAt());

                    return dto;
                });
    }

    // 예약 승인
    @Override
    @Transactional
    public void approveReservation(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약이 없습니다."));

        // 승인 처리는 예약이 유지되는 상태이므로 대기번호를 재정렬하지 않는다.
        reservation.approve();

        sendNotificationSafely(
                reservation.getMember().getUsername(),
                NotificationDTO.builder()
                        .type("RESERVATION_APPROVED")
                        .message("예약이 승인되었습니다.")
                        .url("/reservation/list")
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    // 예약 거절
    @Override
    @Transactional
    public void rejectReservation(Long reservationId) {

        log.info("병원관리자 예약 거절 실행 - reservationId: {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약이 없습니다."));

        if (reservation.getStatus() == ReservationStatus.CANCEL) {
            throw new RuntimeException("이미 취소된 예약입니다.");
        }

        if (reservation.getStatus() == ReservationStatus.DONE) {
            throw new RuntimeException("이미 진료 완료된 예약입니다.");
        }

        // 거절 전 예약의 병원, 날짜, 시간 정보를 저장
        // 거절 후 같은 시간대 예약들의 대기번호를 다시 계산하기 위해 필요하다.
        Long hospitalId = reservation.getHospital().getId();
        LocalDate reserveDate = reservation.getReserveDate();
        LocalTime reserveTime = reservation.getReserveTime();

        log.info("거절 전 대기번호 - reservationId: {}, waitNumber: {}",
                reservation.getId(),
                reservation.getWaitNumber());

        // 예약 상태를 CANCEL로 변경
        reservation.rejectByHospitalManager();

        // 거절된 예약은 대기번호를 0으로 변경
        reservation.changeWaitNumber(0);

        log.info("거절 후 대기번호 0 처리 - reservationId: {}, waitNumber: {}",
                reservation.getId(),
                reservation.getWaitNumber());

        reservationRepository.flush();

        // 같은 병원, 같은 날짜, 같은 시간대 예약들의 대기번호 재정렬
        refreshWaitingQueue(hospitalId, reserveDate, reserveTime);

        sendNotificationSafely(
                reservation.getMember().getUsername(),
                NotificationDTO.builder()
                        .type("RESERVATION_REJECTED")
                        .message("예약이 거절되었습니다.")
                        .url("/reservation/list")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        log.info("병원관리자 예약 거절 완료 - reservationId: {}", reservationId);
    }

    // 실시간 대기열 재정렬
    // 같은 병원 + 같은 날짜 + 같은 시간 예약 중 취소되지 않은 예약을 가져와
    // 생성순으로 1번, 2번, 3번 대기번호를 다시 부여함
    private void refreshWaitingQueue(Long hospitalId, LocalDate reserveDate, LocalTime reserveTime) {

        log.info("병원관리자 refreshWaitingQueue 실행됨 - hospitalId: {}, reserveDate: {}, reserveTime: {}",
                hospitalId,
                reserveDate,
                reserveTime);

        List<Reservation> activeReservations =
                reservationRepository.findActiveReservationsForWaitingQueue(
                        hospitalId,
                        reserveDate,
                        reserveTime,
                        ReservationStatus.CANCEL
                );

        log.info("병원관리자 대기열 재정렬 대상 예약 수: {}", activeReservations.size());

        int number = 1;

        for (Reservation activeReservation : activeReservations) {

            // 진료 완료된 예약은 대기열에서 제외
            if (activeReservation.getStatus() == ReservationStatus.DONE) {
                continue;
            }

            // 새 대기번호 부여
            activeReservation.changeWaitNumber(number);

            log.info("병원관리자 대기번호 변경 대상 - reservationId: {}, username: {}, newWaitNumber: {}",
                    activeReservation.getId(),
                    activeReservation.getMember().getUsername(),
                    number);

            // 해당 예약자에게 실시간 대기번호 변경 알림 전송
            sendNotificationSafely(
                    activeReservation.getMember().getUsername(),
                    NotificationDTO.builder()
                            .type("WAITING_QUEUE_UPDATED")
                            .message("예약 대기번호가 " + number + "번으로 변경되었습니다.")
                            .url("/reservation/list")
                            .reservationId(activeReservation.getId())
                            .waitNumber(number)
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            number++;
        }

        reservationRepository.flush();

        log.info("병원관리자 대기열 재정렬 완료 - hospitalId: {}, date: {}, time: {}",
                hospitalId,
                reserveDate,
                reserveTime);
    }

    // 알림 전송 실패가 예약 상태 변경이나 대기번호 저장을 롤백시키지 않도록 예외를 잡는다.
    private void sendNotificationSafely(String username, NotificationDTO notificationDTO) {
        try {
            notificationService.sendToUser(username, notificationDTO);
        } catch (Exception e) {
            log.warn("SSE 알림 전송 실패 - username: {}, type: {}",
                    username,
                    notificationDTO.getType(),
                    e);
        }
    }
}