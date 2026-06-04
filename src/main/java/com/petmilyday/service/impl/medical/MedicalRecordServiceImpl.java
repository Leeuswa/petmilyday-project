package com.petmilyday.service.impl.medical;

import com.petmilyday.dto.medical.MedicalRecordResponseDTO;
import com.petmilyday.entity.medical.MedicalRecord;
import com.petmilyday.repository.medical.MedicalRecordRepository;
import com.petmilyday.service.medical.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.petmilyday.entity.medical.QMedicalRecord.medicalRecord;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

   private final MedicalRecordRepository medicalRecordRepository;
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




}
