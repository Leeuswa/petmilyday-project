package com.petmilyday.service.impl.reservation;

import com.petmilyday.dto.notification.NotificationDTO;
import com.petmilyday.dto.reservation.ReservationRequestDTO;
import com.petmilyday.dto.reservation.ReservationResponseDTO;
import com.petmilyday.dto.reservation.ReservationSlotDto;
import com.petmilyday.entity.hospital.Hospital;
import com.petmilyday.entity.hospital.HospitalHours;
import com.petmilyday.entity.hospital.HospitalManager;
import com.petmilyday.entity.hospital.HospitalManagerStatus;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.member.PetProfile;
import com.petmilyday.entity.member.Role;
import com.petmilyday.entity.reservation.Reservation;
import com.petmilyday.entity.reservation.ReservationStatus;
import com.petmilyday.repository.hospital.HospitalHoursRepository;
import com.petmilyday.repository.hospital.HospitalManagerRepository;
import com.petmilyday.repository.hospital.HospitalRepository;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.member.PetProfileRepository;
import com.petmilyday.repository.reservation.ReservationRepository;
import com.petmilyday.service.notification.NotificationService;
import com.petmilyday.service.reservation.ReservationService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final HospitalRepository hospitalRepository;
    private final MemberRepository memberRepository;
    private final PetProfileRepository petProfileRepository;
    private final HospitalHoursRepository hospitalHoursRepository;
    private final ModelMapper modelMapper;

    private final NotificationService notificationService;
    private final HospitalManagerRepository hospitalManagerRepository;

    // 예약 등록
    @Override
    @Transactional
    public void reservationRegister(ReservationRequestDTO dto, String loginId) {

        Hospital hospital = hospitalRepository.findById(dto.getHospitalId())
                .orElseThrow(() -> new RuntimeException("병원을 찾을 수 없습니다."));

        Member member = memberRepository.findByUsername(loginId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        PetProfile pet = petProfileRepository.findById(dto.getPetId())
                .orElseThrow(() -> new RuntimeException("반려 동물을 찾을 수 없습니다."));

        // 현재 시간대의 예약 수 조회
        // 이 값을 기준으로 대기번호를 부여한다.
        long currentCount = reservationRepository.countAvailableSlot(
                dto.getHospitalId(),
                dto.getReserveDate(),
                dto.getReserveTime()
        );

        log.info("슬롯 현재 예약 수: {}, 최대: {}", currentCount, hospital.getMaxPerSlot());

        // 같은 회원이 같은 날짜, 같은 시간에 이미 예약했는지 확인
        boolean exists = reservationRepository
                .existsByMemberAndReserveDateAndReserveTimeAndStatusNot(
                        member,
                        dto.getReserveDate(),
                        dto.getReserveTime(),
                        ReservationStatus.CANCEL
                );

        if (exists) {
            throw new RuntimeException("이미 같은 시간 예약이 존재합니다.");
        }

        // 병원에서 정한 시간대 최대 예약 수를 넘으면 예약 불가
        if (currentCount >= hospital.getMaxPerSlot()) {
            throw new RuntimeException("해당 시간대 예약이 마감되었습니다.");
        }

        // 현재 예약 수 + 1을 대기번호로 사용
        int waitNumber = (int) currentCount + 1;

        Reservation reservation = Reservation.builder()
                .member(member)
                .hospital(hospital)
                .pet(pet)
                .reserveDate(dto.getReserveDate())
                .reserveTime(dto.getReserveTime())
                .department(dto.getDepartment())
                .status(ReservationStatus.WAITING)
                .waitNumber(waitNumber)
                .build();

        reservationRepository.save(reservation);

        // 해당 병원의 승인된 병원 관리자에게 새 예약 알림 전송
        List<HospitalManager> managers =
                hospitalManagerRepository.findManagersByHospitalIdAndStatus(
                        hospital.getId(),
                        HospitalManagerStatus.APPROVED
                );

        // hospital_manager 상태가 APPROVED여도 member role이 USER로 꼬여 있을 수 있으므로
        // 실제 권한이 HOSPITAL_ADMIN인 회원에게만 새 예약 알림을 보낸다.
        List<String> managerUsernames = managers.stream()
                .filter(manager -> manager.getMember().getRole() == Role.HOSPITAL_ADMIN)
                .map(manager -> manager.getMember().getUsername())
                .toList();

        log.info("SSE 알림 대상 병원 관리자 username 목록: {}", managerUsernames);

        if (!managerUsernames.isEmpty()) {
            notificationService.sendToUsers(
                    managerUsernames,
                    NotificationDTO.builder()
                            .type("NEW_RESERVATION")
                            .message(hospital.getName() + "에 새 예약이 들어왔습니다.")
                            .url("/hospitalAdmin/reservations")
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        }

        log.info("예약 완료 - 병원: {}, 날짜: {}, 시간: {}, 대기번호: {}",
                hospital.getName(),
                dto.getReserveDate(),
                dto.getReserveTime(),
                waitNumber);
    }

    // 내 예약 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> reservationList(String username) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        List<Reservation> reservations = reservationRepository
                .findByMemberIdOrderByCreatedAtDesc(member.getId());

        log.info("예약 목록 조회 - username: {}, 총 {}건",
                username,
                reservations.size());

        return reservations.stream()
                .map(reservation -> {
                    ReservationResponseDTO dto =
                            modelMapper.map(reservation, ReservationResponseDTO.class);

                    dto.setHospitalName(reservation.getHospital().getName());
                    dto.setPetName(reservation.getPet().getName());
                    dto.setHospitalId(reservation.getHospital().getId());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 내 예약 목록 조회 + 페이징
    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponseDTO> reservationListPage(String username, int page) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        Pageable pageable = PageRequest.of(page, 10);

        Page<Reservation> reservationPage =
                reservationRepository.findByMemberIdOrderByCreatedAtDesc(
                        member.getId(),
                        pageable
                );

        log.info("예약 목록 페이징 조회 - username: {}, 총 {}건",
                username,
                reservationPage.getTotalElements());

        return reservationPage.map(reservation -> {
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

    // 예약 취소
    @Override
    @Transactional
    public void reservationCancel(Long reservationId, String cancelReason) {

        log.info("실시간 대기열 적용된 reservationCancel 실행됨 - reservationId: {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        if (reservation.getStatus() == ReservationStatus.CANCEL) {
            throw new RuntimeException("이미 취소된 예약입니다.");
        }

        if (reservation.getStatus() == ReservationStatus.DONE) {
            throw new RuntimeException("완료된 예약은 취소 할 수 없습니다.");
        }

        // 취소 전 예약의 병원, 날짜, 시간 정보를 저장
        Long hospitalId = reservation.getHospital().getId();
        LocalDate reserveDate = reservation.getReserveDate();
        LocalTime reserveTime = reservation.getReserveTime();

        log.info("취소 전 대기번호 - reservationId: {}, waitNumber: {}",
                reservation.getId(),
                reservation.getWaitNumber());

        // 예약 상태를 CANCEL로 변경
        reservation.cancel(cancelReason);

        // 취소된 예약은 대기번호를 0으로 변경
        reservation.changeWaitNumber(0);

        log.info("취소 후 대기번호 0 처리 - reservationId: {}, waitNumber: {}",
                reservation.getId(),
                reservation.getWaitNumber());

        reservationRepository.flush();

        // 같은 병원, 같은 날짜, 같은 시간대 예약들의 대기번호 재정렬
        refreshWaitingQueue(hospitalId, reserveDate, reserveTime);

        log.info("예약 취소 - reservationId: {}, 사유: {}", reservationId, cancelReason);
    }

    // 예약 가능한 시간 슬롯 조회
    @Override
    @Transactional(readOnly = true)
    public List<ReservationSlotDto> getAvailableSlots(Long hospitalId, LocalDate date) {

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("병원을 찾을 수 없습니다."));

        int dayOfWeek = date.getDayOfWeek().getValue() - 1;

        HospitalHours hospitalHours = hospitalHoursRepository
                .findByHospitalAndDayOfWeek(hospital, dayOfWeek)
                .orElseThrow(() -> new RuntimeException("운영시간이 없습니다."));

        LocalTime openTime = hospitalHours.getOpenTime();
        LocalTime closeTime = hospitalHours.getCloseTime();

        List<ReservationSlotDto> slots = new ArrayList<>();

        LocalTime currentTime = openTime;

        while (currentTime.isBefore(closeTime)) {

            long currentCount = reservationRepository
                    .countByHospitalAndReserveDateAndReserveTimeAndStatusNot(
                            hospital,
                            date,
                            currentTime,
                            ReservationStatus.CANCEL
                    );

            boolean available = currentCount < hospital.getMaxPerSlot();

            ReservationSlotDto slot = ReservationSlotDto.builder()
                    .time(currentTime)
                    .available(available)
                    .currentCount((int) currentCount)
                    .maxCount(hospital.getMaxPerSlot())
                    .build();

            slots.add(slot);

            currentTime = currentTime.plusMinutes(hospital.getSlotIntervalMin());
        }

        return slots;
    }

    // 내 진료기록 조회
    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> myMedicalRecords(String username) {

        List<Reservation> medicalRecords = reservationRepository.findMedicalRecords(
                username,
                ReservationStatus.DONE
        );

        return medicalRecords.stream()
                .map(reservation -> {
                    ReservationResponseDTO dto =
                            modelMapper.map(reservation, ReservationResponseDTO.class);

                    dto.setHospitalId(reservation.getHospital().getId());
                    dto.setHospitalName(reservation.getHospital().getName());
                    dto.setPetName(reservation.getPet().getName());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 내 진료기록 조회 + 페이징
    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponseDTO> myMedicalRecordsPage(String username, int page) {

        Pageable pageable = PageRequest.of(page, 10);

        Page<Reservation> medicalRecordPage =
                reservationRepository.findMedicalRecordsPage(
                        username,
                        ReservationStatus.DONE,
                        pageable
                );

        return medicalRecordPage.map(reservation -> {
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

    // 실시간 대기열 재정렬
    // 같은 병원 + 같은 날짜 + 같은 시간 예약 중 취소되지 않은 예약을 가져와
    // 생성순으로 1번, 2번, 3번 대기번호를 다시 부여함
    private void refreshWaitingQueue(Long hospitalId, LocalDate reserveDate, LocalTime reserveTime) {

        log.info("refreshWaitingQueue 실행됨 - hospitalId: {}, reserveDate: {}, reserveTime: {}",
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

        log.info("대기열 재정렬 대상 예약 수: {}", activeReservations.size());

        int number = 1;

        for (Reservation activeReservation : activeReservations) {

            // 진료 완료된 예약은 대기열에서 제외
            if (activeReservation.getStatus() == ReservationStatus.DONE) {
                continue;
            }

            // 새 대기번호 부여
            activeReservation.changeWaitNumber(number);

            log.info("대기번호 변경 대상 - reservationId: {}, username: {}, newWaitNumber: {}",
                    activeReservation.getId(),
                    activeReservation.getMember().getUsername(),
                    number);

            // 해당 예약자에게 실시간 대기번호 변경 알림 전송
            notificationService.sendToUser(
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

        log.info("대기열 재정렬 완료 - hospitalId: {}, date: {}, time: {}",
                hospitalId,
                reserveDate,
                reserveTime);
    }
}