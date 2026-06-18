package com.petmilyday.service.impl.admin;

import com.petmilyday.dto.admin.AdminHospitalDTO;
import com.petmilyday.entity.hospital.Hospital;
import com.petmilyday.repository.hospital.HospitalRepository;
import com.petmilyday.service.admin.AdminHospitalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class AdminHospitalServiceImpl implements AdminHospitalService {

    private final HospitalRepository hospitalRepository;
    private final ModelMapper modelMapper;

    // 병원 기본정보 등록 - 메인 관리자
    @Override
    public void register(AdminHospitalDTO dto) {

        Hospital hospital = Hospital.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .phone(dto.getPhone())

                // 병원 세부정보 기본값
                // 실제 세부정보는 병원관리자가 수정
                .isEmergency(false)
                .department("미입력")
                .slotIntervalMin(30)
                .maxPerSlot(3)
                .build();

        hospitalRepository.save(hospital);
    }

    // 병원 리스트 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<AdminHospitalDTO> findAll() {

        List<Hospital> hospitalList = hospitalRepository.findAll();

        return hospitalList.stream()
                .map(hospital -> modelMapper.map(hospital, AdminHospitalDTO.class))
                .toList();
    }

    // 병원 리스트 목록 조회 + 페이징
    @Override
    @Transactional(readOnly = true)
    public Page<AdminHospitalDTO> findAllPage(int page) {

        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(Sort.Direction.DESC, "id")
        );

        return hospitalRepository.findAll(pageable)
                .map(hospital -> modelMapper.map(hospital, AdminHospitalDTO.class));
    }

    // 병원 1개 조회
    @Override
    @Transactional(readOnly = true)
    public AdminHospitalDTO findById(Long hospitalId) {

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("병원이 없습니다."));

        return modelMapper.map(hospital, AdminHospitalDTO.class);
    }

    // 병원 기본정보 수정 - 메인 관리자
    @Override
    public void modify(Long hospitalId, AdminHospitalDTO dto) {

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("등록된 병원이 없습니다."));

        hospital.updateBasicInfo(
                dto.getName(),
                dto.getAddress(),
                dto.getLatitude(),
                dto.getLongitude(),
                dto.getPhone()
        );
    }

    // 병원 데이터 삭제
    @Override
    public void remove(Long hospitalId) {
        hospitalRepository.deleteById(hospitalId);
    }
}