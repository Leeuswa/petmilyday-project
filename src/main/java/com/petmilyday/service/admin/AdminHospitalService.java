package com.petmilyday.service.admin;

import com.petmilyday.dto.admin.AdminHospitalDTO;

import java.util.List;

public interface AdminHospitalService {

    //관리자가 병원 데이터 넣기 위한 로직
    void register(AdminHospitalDTO dto);

    //병원 전체 목록
    List<AdminHospitalDTO> findAll();

    //병원 1개 조회
    AdminHospitalDTO findById(Long hospitalId);

    //병원 데이터 수정
    void modify(Long hospitalId, AdminHospitalDTO dto);

    //병원 데이터 삭제
    void remove(Long hospitalId);


}
