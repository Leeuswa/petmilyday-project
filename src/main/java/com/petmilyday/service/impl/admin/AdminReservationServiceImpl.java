package com.petmilyday.service.impl.admin;

import com.petmilyday.dto.admin.AdminReservationDTO;
import com.petmilyday.entity.reservation.Reservation;
import com.petmilyday.entity.reservation.ReservationStatus;
import com.petmilyday.repository.reservation.ReservationRepository;
import com.petmilyday.service.admin.AdminReservationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.petmilyday.entity.reservation.QReservation.reservation;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminReservationServiceImpl implements AdminReservationService {

    private final ReservationRepository reservationRepository;
    private final ModelMapper modelMapper;


    //예약 리스트
    @Override
    @Transactional(readOnly = true)
    public List<AdminReservationDTO> reservationList() {
        return reservationRepository.findAll()
                .stream()
                .map(reservation -> AdminReservationDTO.builder()
                        .id(reservation.getId())
                        .hospitalName(reservation.getHospital().getName())
                                .memberName(reservation.getMember().getName())
                                .reservationDate(reservation.getReserveDate())
                                .reservationTime(reservation.getReserveTime())
                                .status(reservation.getStatus())
                                .build()).toList();

    }

    //예약 상세보기
    @Override
    @Transactional(readOnly = true)
    public AdminReservationDTO reservationDetail(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약 정보가 없습니다."));

        return AdminReservationDTO.builder()
                .id(reservation.getId())
                .hospitalName(reservation.getHospital().getName())
                .memberName(reservation.getMember().getName())
                .reservationDate(reservation.getReserveDate())
                .reservationTime(reservation.getReserveTime())
                .status(reservation.getStatus())
                .build();
    }


    @Override
    public void approveReservation(Long reservationId) {
        Reservation reservation = reservationRepository
                .findById(reservationId)
                .orElseThrow(()-> new RuntimeException("예약 정보가 없습니다."));

        reservation.approve();
    }

    @Override
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약 정보가 없습니다."));

        reservation.adminCancel();

    }
}
