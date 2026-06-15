package com.petmilyday.service.impl.hospital;

import com.petmilyday.dto.admin.AdminHospitalHourDTO;
import com.petmilyday.dto.hospital.HospitalAdminHospitalDTO;
import com.petmilyday.entity.hospital.Hospital;
import com.petmilyday.entity.hospital.HospitalHours;
import com.petmilyday.entity.hospital.HospitalManager;
import com.petmilyday.entity.hospital.HospitalManagerStatus;
import com.petmilyday.entity.member.Member;
import com.petmilyday.repository.hospital.HospitalManagerRepository;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.service.hospital.HospitalAdminHospitalService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HospitalAdminHospitalServiceImpl implements HospitalAdminHospitalService {

    private final MemberRepository memberRepository;
    private final HospitalManagerRepository hospitalManagerRepository;
    private final ModelMapper modelMapper;

    // 병원관리자가 담당하는 병원 조회
    @Override
    @Transactional(readOnly = true)
    public HospitalAdminHospitalDTO findMyHospital(String username) {

        Hospital hospital = getMyHospital(username);

        // 병원 기본정보 + 세부정보는 ModelMapper로 변환
        HospitalAdminHospitalDTO dto =
                modelMapper.map(hospital, HospitalAdminHospitalDTO.class);

        // 운영시간 목록은 요일 순서대로 정렬 후 DTO로 변환
        List<AdminHospitalHourDTO> hours = hospital.getHours().stream()
                .sorted(Comparator.comparing(HospitalHours::getDayOfWeek))
                .map(hour -> modelMapper.map(hour, AdminHospitalHourDTO.class))
                .toList();

        dto.setHours(hours);

        return dto;
    }

    // 병원관리자가 담당 병원 세부정보 수정
    @Override
    public void modifyMyHospital(String username, HospitalAdminHospitalDTO dto) {

        Hospital hospital = getMyHospital(username);

        // 병원 세부정보 수정
        hospital.updateDetailInfo(
                dto.getIsEmergency(),
                dto.getDepartment(),
                dto.getSlotIntervalMin(),
                dto.getMaxPerSlot()
        );

        // 기존 운영시간 삭제
        hospital.getHours().clear();

        // 운영시간 다시 등록
        if (dto.getHours() != null) {
            for (AdminHospitalHourDTO hourDTO : dto.getHours()) {

                HospitalHours hour = HospitalHours.builder()
                        .hospital(hospital)
                        .dayOfWeek(hourDTO.getDayOfWeek())
                        .openTime(hourDTO.getOpenTime())
                        .closeTime(hourDTO.getCloseTime())
                        .isClosed(hourDTO.getIsClosed())
                        .build();

                hospital.getHours().add(hour);
            }
        }
    }

    // 로그인한 병원관리자의 담당 병원 찾기
    private Hospital getMyHospital(String username) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원이 없습니다."));

        HospitalManager hospitalManager = hospitalManagerRepository
                .findByMemberIdAndStatus(member.getId(), HospitalManagerStatus.APPROVED)
                .orElseThrow(() -> new RuntimeException("승인된 병원 관리자가 아닙니다."));

        return hospitalManager.getHospital();
    }
}