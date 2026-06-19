package com.petmilyday.service.impl.medical;

import com.petmilyday.dto.medical.MedicalRecordResponseDTO;
import com.petmilyday.entity.medical.MedicalRecord;
import com.petmilyday.entity.reservation.Reservation;
import com.petmilyday.entity.reservation.ReservationStatus;
import com.petmilyday.repository.medical.MedicalRecordRepository;
import com.petmilyday.repository.reservation.ReservationRepository;
import com.petmilyday.service.medical.MedicalRecordService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.petmilyday.entity.medical.QMedicalRecord.medicalRecord;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicalRecordServiceImpl implements MedicalRecordService {

   private final MedicalRecordRepository medicalRecordRepository;
    private final ReservationRepository reservationRepository;
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
    @Transactional
    public void register(MedicalRecordResponseDTO dto) {
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new RuntimeException("예약 정보가 없습니다."));

        if (reservation.getStatus() != ReservationStatus.APPROVED) {
            throw new RuntimeException("승인된 예약만 진료기록을 작성할 수 있습니다.");
        }

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

        reservation.done();
    }
}
