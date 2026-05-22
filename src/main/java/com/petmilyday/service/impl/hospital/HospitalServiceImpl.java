package com.petmilyday.service.impl.hospital;

import com.petmilyday.dto.hospital.HospitalRequestDTO;
import com.petmilyday.dto.hospital.HospitalResponseDTO;
import com.petmilyday.entity.hospital.Hospital;
import com.petmilyday.repository.hospital.HospitalRepository;
import com.petmilyday.service.hospital.HospitalService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HospitalServiceImpl implements HospitalService {

    private final HospitalRepository hospitalRepository;
    private final ModelMapper modelMapper;

    //병원 목록 조회
    @Override
    public List<HospitalResponseDTO> hospitalList(HospitalRequestDTO requestDTO) {
        // 병원 데이터 가져오기
        List<Hospital> hospitals = hospitalRepository.findAll();
        //Entity -> DTO 변경
        return hospitals.stream()
                .map(hospital -> modelMapper.map(hospital, HospitalResponseDTO.class))
                .collect(Collectors.toList());
    }

    //병원 1개만 조회
    @Override
    public HospitalResponseDTO hospitalReadOne(Long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId).orElseThrow(
                ()-> new RuntimeException("병원을 찾을 수 없습니다."));
        return modelMapper.map(hospital,HospitalResponseDTO.class);

    }
}
