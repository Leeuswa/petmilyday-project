package com.petmilyday.service.impl.medical;

import com.petmilyday.dto.medical.MedicalRecordResponseDTO;
import com.petmilyday.entity.hospital.HospitalManager;
import com.petmilyday.entity.hospital.HospitalManagerStatus;
import com.petmilyday.entity.medical.MedicalRecord;
import com.petmilyday.entity.medical.Vaccination;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.reservation.Reservation;
import com.petmilyday.entity.reservation.ReservationStatus;
import com.petmilyday.repository.hospital.HospitalManagerRepository;
import com.petmilyday.repository.medical.MedicalRecordRepository;
import com.petmilyday.repository.medical.VaccinationRepository;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.repository.reservation.ReservationRepository;
import com.petmilyday.service.medical.MedicalRecordService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.petmilyday.entity.medical.QMedicalRecord.medicalRecord;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicalRecordServiceImpl implements MedicalRecordService {

   private final MedicalRecordRepository medicalRecordRepository;
    private final ReservationRepository reservationRepository;
    private final VaccinationRepository vaccinationRepository;
    private final MemberRepository memberRepository;
    private final HospitalManagerRepository hospitalManagerRepository;
   private final ModelMapper modelMapper;

    @Override
    public List<MedicalRecordResponseDTO> medicalRecordList(Long petId) {
        List<MedicalRecord> medicalRecordList = medicalRecordRepository.findByPetIdOrderByCreatedAtDesc(petId);

        return medicalRecordList.stream()
                .map(medicalRecord -> {
                    MedicalRecordResponseDTO dto = modelMapper.map(medicalRecord, MedicalRecordResponseDTO.class);
                    dto.setHospitalName(medicalRecord.getReservation().getHospital().getName());
                    dto.setPetId(medicalRecord.getPet().getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public MedicalRecordResponseDTO prepareRegisterForm(Long reservationId, String username) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약 정보가 없습니다."));

        validateHospitalOwnership(reservation, username);

        if (reservation.getStatus() != ReservationStatus.APPROVED) {
            throw new RuntimeException("승인된 예약만 진료기록을 작성할 수 있습니다.");
        }

        validateReservationDateReached(reservation);

        return MedicalRecordResponseDTO.builder()
                .reservationId(reservationId)
                .visitDate(LocalDate.now())
                .build();
    }

    @Override
    @Transactional
    public void register(MedicalRecordResponseDTO dto, String username) {
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new RuntimeException("예약 정보가 없습니다."));

        validateHospitalOwnership(reservation, username);
        validateVisitDateNotFuture(dto.getVisitDate());

        if (reservation.getStatus() != ReservationStatus.APPROVED) {
            throw new RuntimeException("승인된 예약만 진료기록을 작성할 수 있습니다.");
        }

        validateReservationDateReached(reservation);

        MedicalRecord medicalRecord = MedicalRecord.builder()
                .reservation(reservation)
                .pet(reservation.getPet())
                .diagnosis(dto.getDiagnosis())
                .treatment(dto.getTreatment())
                .prescription(dto.getPrescription())
                .vaccinated(dto.getVaccinated() != null ? dto.getVaccinated() : false)
                .vaccineName(dto.getVaccineName())
                .memo(dto.getMemo())
                .pdfUrl(dto.getPdfUrl())
                .visitDate(dto.getVisitDate())
                .build();

        medicalRecordRepository.save(medicalRecord);

        // 예방접종을 진행한 경우, 예방접종 기록도 함께 생성
        if (Boolean.TRUE.equals(medicalRecord.getVaccinated())
                && medicalRecord.getVaccineName() != null
                && !medicalRecord.getVaccineName().isBlank()) {

            Vaccination vaccination = Vaccination.builder()
                    .pet(reservation.getPet())
                    .vaccineName(medicalRecord.getVaccineName())
                    .vaccinatedDate(medicalRecord.getVisitDate())
                    .nextDate(dto.getNextVaccinationDate() != null
                            ? dto.getNextVaccinationDate()
                            : medicalRecord.getVisitDate().plusYears(1))
                    .build();

            vaccinationRepository.save(vaccination);
        }

        reservation.done();
    }

    @Override
    public MedicalRecordResponseDTO prepareModifyForm(Long reservationId, String username) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약 정보가 없습니다."));

        validateHospitalOwnership(reservation, username);

        MedicalRecord medicalRecord = medicalRecordRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new RuntimeException("진료기록이 없습니다."));

        return MedicalRecordResponseDTO.builder()
                .id(medicalRecord.getId())
                .reservationId(reservationId)
                .diagnosis(medicalRecord.getDiagnosis())
                .treatment(medicalRecord.getTreatment())
                .prescription(medicalRecord.getPrescription())
                .vaccinated(medicalRecord.getVaccinated())
                .vaccineName(medicalRecord.getVaccineName())
                .memo(medicalRecord.getMemo())
                .pdfUrl(medicalRecord.getPdfUrl())
                .visitDate(medicalRecord.getVisitDate())
                .build();
    }

    @Override
    @Transactional
    public void modify(MedicalRecordResponseDTO dto, String username) {
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new RuntimeException("예약 정보가 없습니다."));

        validateHospitalOwnership(reservation, username);
        validateVisitDateNotFuture(dto.getVisitDate());

        MedicalRecord medicalRecord = medicalRecordRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("진료기록이 없습니다."));

        medicalRecord.update(
                dto.getDiagnosis(),
                dto.getTreatment(),
                dto.getPrescription(),
                dto.getVaccinated(),
                dto.getVaccineName(),
                dto.getMemo(),
                dto.getPdfUrl(),
                dto.getVisitDate()
        );
    }

    // 로그인한 병원관리자가 해당 예약의 소유 병원인지 검증
    private void validateHospitalOwnership(Reservation reservation, String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원이 없습니다."));

        HospitalManager hospitalManager = hospitalManagerRepository
                .findByMemberIdAndStatus(member.getId(), HospitalManagerStatus.APPROVED)
                .orElseThrow(() -> new RuntimeException("승인된 병원 관리자가 아닙니다."));

        if (!hospitalManager.getHospital().getId().equals(reservation.getHospital().getId())) {
            throw new RuntimeException("해당 예약에 접근할 권한이 없습니다.");
        }
    }

    // 진료일이 오늘보다 미래인지 검증 (서버단 검증)
    private void validateVisitDateNotFuture(LocalDate visitDate) {
        if (visitDate != null && visitDate.isAfter(LocalDate.now())) {
            throw new RuntimeException("미래 날짜로는 진료기록을 작성할 수 없습니다.");
        }
    }

    // 예약 날짜가 아직 되지 않았으면 진료기록을 작성할 수 없도록 검증
    private void validateReservationDateReached(Reservation reservation) {
        if (reservation.getReserveDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("예약 날짜 이전에는 진료기록을 작성할 수 없습니다.");
        }
    }
}
