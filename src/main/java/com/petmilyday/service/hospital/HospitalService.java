package com.petmilyday.service.hospital;

import com.petmilyday.dto.hospital.HospitalRequestDTO;
import com.petmilyday.dto.hospital.HospitalResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface HospitalService {

    //병원 목록 조회
    List<HospitalResponseDTO> hospitalList(HospitalRequestDTO requestDTO);

    //병원 목록 조회 페이징 (username이 있으면 검색어가 없을 때 회원 주소를 기준 좌표로 사용)
    Page<HospitalResponseDTO> hospitalListPage(HospitalRequestDTO requestDTO, int page, String username);

    //병원 상세 조회
    HospitalResponseDTO hospitalReadOne(Long hospitalId);
}