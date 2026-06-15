package com.petmilyday.service.impl.hospital;

import com.petmilyday.dto.reservation.ReservationResponseDTO;
import com.petmilyday.entity.hospital.HospitalManager;
import com.petmilyday.entity.hospital.HospitalManagerStatus;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.reservation.Reservation;
import com.petmilyday.repository.hospital.HospitalManagerRepository;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.reservation.ReservationRepository;
import com.petmilyday.service.hospital.HospitalAdminReservationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HospitalAdminReservationServiceImpl implements HospitalAdminReservationService {

    private final MemberRepository memberRepository;
    private final HospitalManagerRepository hospitalManagerRepository;
    private final ReservationRepository reservationRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<ReservationResponseDTO> reservationList(String username) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원이 없습니다."));

        HospitalManager hospitalManager = hospitalManagerRepository
                .findByMemberIdAndStatus(member.getId(), HospitalManagerStatus.APPROVED)
                .orElseThrow(() -> new RuntimeException("승인된 병원 관리자가 아닙니다."));

        Long hospitalId = hospitalManager.getHospital().getId();

        List<Reservation> reservations =
                reservationRepository.findHospitalReservations(hospitalId);

        return reservations.stream()
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

    @Override
    @Transactional
    public void approveReservation(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약이 없습니다."));

        reservation.approve();
    }

    @Override
    @Transactional
    public void rejectReservation(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약이 없습니다."));

        reservation.adminCancel();
    }
}