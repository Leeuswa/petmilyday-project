package com.petmilyday.service.medical;

import com.petmilyday.dto.medical.VaccinationResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface VaccinationService {

    List<VaccinationResponseDTO> vaccinationList(Long petId);

    // 내 동물 예방접종 기록 조회
    List<VaccinationResponseDTO> myVaccinationList(String username);

    // 내 동물 예방접종 기록 조회 + 페이징
    Page<VaccinationResponseDTO> myVaccinationListPage(String username, int page);
}