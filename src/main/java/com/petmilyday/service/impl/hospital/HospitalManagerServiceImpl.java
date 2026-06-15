package com.petmilyday.service.impl.hospital;

import com.petmilyday.dto.hospital.HospitalManagerDTO;
import com.petmilyday.entity.hospital.Hospital;
import com.petmilyday.entity.hospital.HospitalManager;
import com.petmilyday.entity.hospital.HospitalManagerStatus;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.member.Role;
import com.petmilyday.repository.hospital.HospitalManagerRepository;
import com.petmilyday.repository.hospital.HospitalRepository;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.service.hospital.HospitalManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class HospitalManagerServiceImpl implements HospitalManagerService {

    private final HospitalManagerRepository hospitalManagerRepository;
    private final MemberRepository memberRepository;
    private final HospitalRepository hospitalRepository;

    @Override
    public void requestManager(String username, HospitalManagerDTO dto) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원이 없습니다."));

        if(member.getRole() != Role.USER){
            throw new RuntimeException("일반 회원만 병원 관리자 신청이 가능합니다.");
        }

        Hospital hospital = hospitalRepository.findById(dto.getHospitalId())
                .orElseThrow(() -> new RuntimeException("병원이 없습니다."));

        boolean alreadyRequested =
                hospitalManagerRepository.existsByMemberIdAndHospitalId(
                        member.getId(),
                        hospital.getId()
                );

        if (alreadyRequested) {
            throw new RuntimeException("이미 해당 병원 관리자 신청을 했습니다.");
        }

        HospitalManager hospitalManager = HospitalManager.builder()
                .member(member)
                .hospital(hospital)
                .managerName(dto.getManagerName())
                .managerPhone(dto.getManagerPhone())
                .businessNumber(dto.getBusinessNumber())
                .status(HospitalManagerStatus.WAITING)
                .build();

        hospitalManagerRepository.save(hospitalManager);
    }


    @Override
    @Transactional(readOnly = true)
    public List<HospitalManager> waitingList() {
        return hospitalManagerRepository.findByStatus(HospitalManagerStatus.WAITING);
    }

    @Override
    public void approveManager(Long hospitalManagerId) {
        HospitalManager hospitalManager = hospitalManagerRepository.findById(hospitalManagerId)
                .orElseThrow(() -> new RuntimeException("신청 정보가 없습니다."));

        hospitalManager.approve();
        hospitalManager.getMember().changeRole(Role.HOSPITAL_ADMIN);
    }

    @Override
    public void rejectManager(Long hospitalManagerId) {
        HospitalManager hospitalManager = hospitalManagerRepository.findById(hospitalManagerId)
                .orElseThrow(() -> new RuntimeException("신청 정보가 없습니다."));

        hospitalManager.reject();
    }
}
