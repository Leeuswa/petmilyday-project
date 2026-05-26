package com.petmilyday.service.hospital;

import com.petmilyday.dto.hospital.HospitalRequestDTO;
import com.petmilyday.dto.hospital.HospitalResponseDTO;

import java.util.List;

public interface HospitalService {

    //병원 목록 조회
    List<HospitalResponseDTO> hospitalList(HospitalRequestDTO requestDTO);

    //병원 상세 조회
    HospitalResponseDTO  hospitalReadOne(Long hospitalId);
}
