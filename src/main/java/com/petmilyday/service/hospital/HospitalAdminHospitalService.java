package com.petmilyday.service.hospital;

import com.petmilyday.dto.hospital.HospitalAdminHospitalDTO;

public interface HospitalAdminHospitalService {

    //병원 조회
    HospitalAdminHospitalDTO findMyHospital(String username);
    //병원 세부내용 수정
    void modifyMyHospital(String username, HospitalAdminHospitalDTO dto);
}