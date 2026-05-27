package com.petmilyday.service.medical;

import com.petmilyday.dto.medical.MedicalRecordResponseDTO;

import java.util.List;

public interface MedicalRecordService {
    List<MedicalRecordResponseDTO> medicalRecordList(Long petId);
}
