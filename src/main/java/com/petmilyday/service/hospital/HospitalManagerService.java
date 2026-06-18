package com.petmilyday.service.hospital;

import com.petmilyday.dto.hospital.HospitalManagerDTO;
import com.petmilyday.entity.hospital.HospitalManager;
import org.springframework.data.domain.Page;

import java.util.List;

public interface HospitalManagerService {

    //병원 관리자 신청
    void requestManager(String username, HospitalManagerDTO dto);

    //병원 관리자 신청한 일반 회원 리스트
    List<HospitalManager> waitingList();

    //병원 관리자 신청한 일반 회원 리스트 + 페이징
    Page<HospitalManager> waitingListPage(int page);

    //병원 관리자 승인
    void approveManager(Long hospitalManagerId);

    //병원 관리자 승인 거부
    void rejectManager(Long hospitalManagerId);
}