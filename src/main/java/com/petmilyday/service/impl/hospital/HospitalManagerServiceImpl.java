package com.petmilyday.service.impl.hospital;

import com.petmilyday.dto.hospital.HospitalManagerDTO;
import com.petmilyday.dto.notification.NotificationDTO;
import com.petmilyday.entity.hospital.Hospital;
import com.petmilyday.entity.hospital.HospitalManager;
import com.petmilyday.entity.hospital.HospitalManagerStatus;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.member.Role;
import com.petmilyday.repository.hospital.HospitalManagerRepository;
import com.petmilyday.repository.hospital.HospitalRepository;
import com.petmilyday.repository.member.MemberRepository;
import com.petmilyday.service.hospital.HospitalManagerService;
import com.petmilyday.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class HospitalManagerServiceImpl implements HospitalManagerService {

    private final HospitalManagerRepository hospitalManagerRepository;
    private final MemberRepository memberRepository;
    private final HospitalRepository hospitalRepository;
    private final NotificationService notificationService;

    @Override
    public void requestManager(String username, HospitalManagerDTO dto) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("회원이 없습니다."));

        if (member.getRole() != Role.USER) {
            throw new RuntimeException("일반 회원만 병원 관리자 신청이 가능합니다.");
        }

        Hospital hospital = hospitalRepository.findById(dto.getHospitalId())
                .orElseThrow(() -> new RuntimeException("병원이 없습니다."));

        // 기존 신청 기록 조회
        HospitalManager existingManager =
                hospitalManagerRepository.findByMemberIdAndHospitalId(
                        member.getId(),
                        hospital.getId()
                ).orElse(null);

        if (existingManager != null) {

            // 이미 대기 중이면 다시 신청 불가
            if (existingManager.getStatus() == HospitalManagerStatus.WAITING) {
                throw new RuntimeException("이미 해당 병원 관리자 신청을 했습니다.");
            }

            // 이미 승인된 경우 다시 신청 불가
            if (existingManager.getStatus() == HospitalManagerStatus.APPROVED) {
                throw new RuntimeException("이미 승인된 병원 관리자입니다.");
            }

            // 거절된 경우에는 기존 데이터를 다시 WAITING으로 변경
            if (existingManager.getStatus() == HospitalManagerStatus.REJECTED) {
                existingManager.reRequest(
                        dto.getManagerName(),
                        dto.getManagerPhone(),
                        dto.getBusinessNumber()
                );

                sendAdminNotification(member, hospital);
                return;
            }
        }

        // 기존 신청 기록이 없으면 새로 신청
        HospitalManager hospitalManager = HospitalManager.builder()
                .member(member)
                .hospital(hospital)
                .managerName(dto.getManagerName())
                .managerPhone(dto.getManagerPhone())
                .businessNumber(dto.getBusinessNumber())
                .status(HospitalManagerStatus.WAITING)
                .build();

        hospitalManagerRepository.save(hospitalManager);

        sendAdminNotification(member, hospital);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HospitalManager> waitingList() {
        return hospitalManagerRepository.findByStatusWithMemberAndHospital(
                HospitalManagerStatus.WAITING
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HospitalManager> waitingListPage(int page) {

        Pageable pageable = PageRequest.of(page, 10);

        return hospitalManagerRepository.findByStatusWithMemberAndHospitalPage(
                HospitalManagerStatus.WAITING,
                pageable
        );
    }

    @Override
    public void approveManager(Long hospitalManagerId) {
        HospitalManager hospitalManager = hospitalManagerRepository.findById(hospitalManagerId)
                .orElseThrow(() -> new RuntimeException("신청 정보가 없습니다."));

        hospitalManager.approve();
        hospitalManager.getMember().changeRole(Role.HOSPITAL_ADMIN);

        notificationService.sendToUser(
                hospitalManager.getMember().getUsername(),
                NotificationDTO.builder()
                        .type("HOSPITAL_MANAGER_APPROVED")
                        .message(hospitalManager.getHospital().getName()
                                + " 병원 관리자 신청이 승인되었습니다. 권한 적용을 위해 다시 로그인해 주세요.")
                        .url("/member/logout")
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    @Override
    public void rejectManager(Long hospitalManagerId) {
        HospitalManager hospitalManager = hospitalManagerRepository.findById(hospitalManagerId)
                .orElseThrow(() -> new RuntimeException("신청 정보가 없습니다."));

        hospitalManager.reject();

        notificationService.sendToUser(
                hospitalManager.getMember().getUsername(),
                NotificationDTO.builder()
                        .type("HOSPITAL_MANAGER_REJECTED")
                        .message("병원 관리자 신청이 거절되었습니다.")
                        .url("/hospital/list")
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    // 메인 관리자에게 병원 관리자 신청 알림 보내기
    private void sendAdminNotification(Member member, Hospital hospital) {
        List<String> adminUsernames = memberRepository.findByRole(Role.ADMIN)
                .stream()
                .map(Member::getUsername)
                .toList();

        log.info("병원 관리자 신청 SSE 알림 대상 메인 관리자 username 목록: {}", adminUsernames);

        notificationService.sendToUsers(
                adminUsernames,
                NotificationDTO.builder()
                        .type("HOSPITAL_MANAGER_REQUEST")
                        .message(member.getName() + "님이 " + hospital.getName() + " 병원 관리자 신청을 했습니다.")
                        .url("/admin/hospital-managers")
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }
}