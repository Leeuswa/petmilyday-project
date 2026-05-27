package com.petmilyday.service.medical;


import com.petmilyday.dto.medical.VaccinationResponseDTO;

import java.util.List;

public interface VaccinationService {
    List<VaccinationResponseDTO> vaccinationList(Long petId);
}
