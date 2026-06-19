package com.petmilyday.service.impl.hospital;

import com.petmilyday.dto.hospital.HospitalHoursDTO;
import com.petmilyday.dto.hospital.HospitalRequestDTO;
import com.petmilyday.dto.hospital.HospitalResponseDTO;
import com.petmilyday.entity.hospital.Hospital;
import com.petmilyday.repository.hospital.HospitalRepository;
import com.petmilyday.service.hospital.HospitalService;
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
public class HospitalServiceImpl implements HospitalService {

    private final HospitalRepository hospitalRepository;
    private final ModelMapper modelMapper;

    // 병원 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<HospitalResponseDTO> hospitalList(HospitalRequestDTO requestDTO) {

        List<Hospital> hospitals = hospitalRepository.searchHospitals(requestDTO);

        return hospitals.stream()
                .map(hospital -> modelMapper.map(hospital, HospitalResponseDTO.class))
                .collect(Collectors.toList());
    }

    // 병원 목록 조회 + 페이징
    @Override
    @Transactional(readOnly = true)
    public Page<HospitalResponseDTO> hospitalListPage(HospitalRequestDTO requestDTO, int page) {

        Pageable pageable = PageRequest.of(page, 9);

        return hospitalRepository.searchHospitalsPage(requestDTO, pageable)
                .map(hospital -> modelMapper.map(hospital, HospitalResponseDTO.class));
    }

    // 병원 1개만 조회
    @Override
    @Transactional(readOnly = true)
    public HospitalResponseDTO hospitalReadOne(Long hospitalId) {

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("병원을 찾을 수 없습니다."));

        // 운영시간 변환
        List<HospitalHoursDTO> hoursDTOList = hospital.getHours().stream()
                .map(h -> HospitalHoursDTO.builder()
                        .dayOfWeek(h.getDayOfWeek())
                        .openTime(h.getOpenTime())
                        .closeTime(h.getCloseTime())
                        .isClosed(h.getIsClosed())
                        .build())
                .collect(Collectors.toList());

        // 이미지 URL 리스트
        List<String> imageUrls = hospital.getImages().stream()
                .map(img -> img.getImgUrl())
                .collect(Collectors.toList());

        return HospitalResponseDTO.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .latitude(hospital.getLatitude())
                .longitude(hospital.getLongitude())
                .phone(hospital.getPhone())
                .isEmergency(hospital.getIsEmergency())
                .department(hospital.getDepartment())
                .rating(hospital.getRating())
                .slotIntervalMin(hospital.getSlotIntervalMin())
                .maxPerSlot(hospital.getMaxPerSlot())
                .hours(hoursDTOList)
                .imageUrls(imageUrls)
                .build();
    }
}