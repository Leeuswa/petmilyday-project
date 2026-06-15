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

    @Override
    @Transactional
    public void reservationRegister(ReservationRequestDTO dto, String loginId) {

        Hospital hospital = hospitalRepository.findById(dto.getHospitalId())
                .orElseThrow(() -> new RuntimeException("병원을 찾을 수 없습니다."));

        Member member = memberRepository.findByUsername(loginId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        PetProfile pet = petProfileRepository.findById(dto.getPetId())
                .orElseThrow(() -> new RuntimeException("반려 동물을 찾을 수 없습니다."));

        long currentCount = reservationRepository.countAvailableSlot(
                dto.getHospitalId(),
                dto.getReserveDate(),
                dto.getReserveTime()
        );

        log.info("슬롯 현재 예약 수: {}, 최대: {}", currentCount, hospital.getMaxPerSlot());

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

        if (currentCount >= hospital.getMaxPerSlot()) {
            throw new RuntimeException("해당 시간대 예약이 마감되었습니다.");
        }

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

        List<HospitalManager> managers =
                hospitalManagerRepository.findManagersByHospitalIdAndStatus(
                        hospital.getId(),
                        HospitalManagerStatus.APPROVED
                );

        List<String> managerUsernames = managers.stream()
                .map(manager -> manager.getMember().getUsername())
                .toList();

        log.info("SSE 알림 대상 병원 관리자 username 목록: {}", managerUsernames);

        notificationService.sendToUsers(
                managerUsernames,
                NotificationDTO.builder()
                        .type("NEW_RESERVATION")
                        .message(hospital.getName() + "에 새 예약이 들어왔습니다.")
                        .url("/hospitalAdmin/reservations")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        log.info("예약 완료 - 병원: {}, 날짜: {}, 시간: {}, 대기번호: {}",
                hospital.getName(),
                dto.getReserveDate(),
                dto.getReserveTime(),
                waitNumber);
    }

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

    @Override
    @Transactional
    public void reservationCancel(Long reservationId, String cancelReason) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        if (reservation.getStatus() == ReservationStatus.CANCEL) {
            throw new RuntimeException("이미 취소된 예약입니다.");
        }

        if (reservation.getStatus() == ReservationStatus.DONE) {
            throw new RuntimeException("완료된 예약은 취소 할 수 없습니다.");
        }

        reservation.cancel(cancelReason);

        log.info("예약 취소 - reservationId: {}, 사유: {}", reservationId, cancelReason);
    }

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
}