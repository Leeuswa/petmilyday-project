package com.petmilyday.service.impl.medical;

import com.petmilyday.dto.medical.VaccinationResponseDTO;
import com.petmilyday.entity.medical.Vaccination;
import com.petmilyday.repository.medical.VaccinationRepository;
import com.petmilyday.service.medical.VaccinationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VaccinationServiceImpl implements VaccinationService {
    private final VaccinationRepository vaccinationRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<VaccinationResponseDTO> vaccinationList(Long petId) {
        List<Vaccination> list = vaccinationRepository.findByPetIdOrderByVaccinatedDateDesc(petId);

        return list.stream()
                .map(vaccination -> {
                    VaccinationResponseDTO dto = modelMapper.map(vaccination, VaccinationResponseDTO.class);
                    dto.setPetName(vaccination.getPet().getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
