package com.petmilyday.entity.hospital;

import com.petmilyday.entity.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HospitalManager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //신청한 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    //신청한 병원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    //신청 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private  HospitalManagerStatus status;

    // 담당자 이름
    private String managerName;

    // 담당자 연락처
    private String managerPhone;

    // 사업자번호
    private String businessNumber;

    public void approve() {
        this.status = HospitalManagerStatus.APPROVED;
    }

    public void reject() {
        this.status = HospitalManagerStatus.REJECTED;
    }

}
