package com.petmilyday.service.medical;

import com.petmilyday.dto.medical.MedicalRecordResponseDTO;

import java.util.List;

public interface MedicalRecordService {
    List<MedicalRecordResponseDTO> medicalRecordList(Long petId);

    // 병원관리자 진료기록 작성 폼 준비 (소유 병원 검증 포함)
    MedicalRecordResponseDTO prepareRegisterForm(Long reservationId, String username);

    void register(MedicalRecordResponseDTO dto, String username);

    // 병원관리자 진료기록 수정 폼 준비 (소유 병원 검증 포함)
    MedicalRecordResponseDTO prepareModifyForm(Long reservationId, String username);

    void modify(MedicalRecordResponseDTO dto, String username);
}
