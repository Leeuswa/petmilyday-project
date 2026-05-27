package com.petmilyday.service.impl.reservation;

import com.petmilyday.dto.reservation.ReservationRequestDTO;
import com.petmilyday.dto.reservation.ReservationResponseDTO;
import com.petmilyday.entity.hospital.Hospital;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.member.PetProfile;
import com.petmilyday.entity.reservation.Reservation;
import com.petmilyday.repository.hospital.HospitalRepository;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.member.PetProfileRepository;
import com.petmilyday.repository.reservation.ReservationRepository;
import com.petmilyday.service.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ModelMapper modelMapper;


    @Override
    public void reservationRegister(ReservationRequestDTO dto) {
        //병원 조회
        Hospital hospital = hospitalRepository.findById(dto.getHospitalId())
                .orElseThrow(() -> new RuntimeException("병원을 찾을 수 없습니다."));
        //멤버 조회
        Member member = memberRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        //반려동물 조회
        PetProfile pet = petProfileRepository.findById(dto.getPetId())
                .orElseThrow(() -> new RuntimeException("반려 동물을 찾을 수 없습니다."));

        //슬롯 중복 체크
        long currentCount = reservationRepository.countAvailableSlot(
                dto.getHospitalId(), dto.getReserveDate(),dto.getReserveTime());
        log.info("슬롯 현재 예약 수: {}, 최대: {}", currentCount, hospital.getMaxPerSlot());

        if(currentCount >= hospital.getMaxPerSlot()){
            throw new RuntimeException("해당 시간대 예약이 마감되었습니다.");
        }

        //대기 시간 부여
        int waitNumber = (int) currentCount + 1;

        //예약 저장
        Reservation reservation = Reservation.builder()
                .member(member)
                .hospital(hospital)
                .pet(pet)
                .reserveDate(dto.getReserveDate())
                .reserveTime(dto.getReserveTime())
                .department(dto.getDepartment())
                .status("WAITING")
                .waitNumber(waitNumber)
                .build();
        reservationRepository.save(reservation);
        log.info("예약 완료 - 병원: {}, 날짜: {}, 시간: {}, 대기번호: {}",
                hospital.getName(), dto.getReserveDate(), dto.getReserveTime(), waitNumber);
    }


    @Override
    public List<ReservationResponseDTO> reservationList(Long memberId) {
        List<Reservation> reservations = reservationRepository
                .findByMemberIdOrderByCreatedAtDesc(memberId);

        log.info("예약 목록 조회 - memberId: {}, 총 {}건", memberId, reservations.size());

        return reservations.stream()
                .map(reservation -> {
                    ReservationResponseDTO dto = modelMapper.map(reservation,ReservationResponseDTO.class);
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
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                () -> new RuntimeException("예약을 찾을 수 없습니다."));
        if (reservation.getStatus().equals("CANCEL")){
            throw new RuntimeException("이미 취소된 예약입니다.");
        }
        if(reservation.getStatus().equals("DONE")){
            throw new RuntimeException("완료된 예약은 취소 할 수 없습니다.");
        }
        reservation.cancel(cancelReason);
        log.info("예약 취소 - reservationId: {}, 사유: {}", reservationId, cancelReason);


    }

}
