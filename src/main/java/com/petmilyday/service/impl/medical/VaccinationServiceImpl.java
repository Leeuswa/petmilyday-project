package com.petmilyday.service.impl.medical;

import com.petmilyday.dto.medical.VaccinationResponseDTO;
import com.petmilyday.entity.medical.Vaccination;
import com.petmilyday.repository.medical.VaccinationRepository;
import com.petmilyday.service.medical.VaccinationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class VaccinationServiceImpl implements VaccinationService {

    private final VaccinationRepository vaccinationRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<VaccinationResponseDTO> vaccinationList(Long petId) {

        List<Vaccination> list = vaccinationRepository.findByPetIdOrderByVaccinatedDateDesc(petId);

        return list.stream()
                .map(vaccination -> {
                    VaccinationResponseDTO dto =
                            modelMapper.map(vaccination, VaccinationResponseDTO.class);

                    dto.setPetId(vaccination.getPet().getId());
                    dto.setPetName(vaccination.getPet().getName());
                    dto.setSpecies(vaccination.getPet().getSpecies());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VaccinationResponseDTO> myVaccinationList(String username) {

        List<Vaccination> vaccinationList =
                vaccinationRepository.findMyVaccinations(username);

        return vaccinationList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VaccinationResponseDTO> myVaccinationListPage(String username, int page) {

        Pageable pageable = PageRequest.of(page, 10);

        return vaccinationRepository.findMyVaccinationsPage(username, pageable)
                .map(this::toDTO);
    }

    private VaccinationResponseDTO toDTO(Vaccination vaccination) {

        return VaccinationResponseDTO.builder()
                .id(vaccination.getId())
                .petId(vaccination.getPet().getId())
                .petName(vaccination.getPet().getName())
                .species(vaccination.getPet().getSpecies())
                .vaccineName(vaccination.getVaccineName())
                .vaccinatedDate(vaccination.getVaccinatedDate())
                .nextDate(vaccination.getNextDate())
                .build();
    }
}