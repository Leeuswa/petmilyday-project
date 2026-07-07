package com.petmilyday.entity.reservation;

import com.petmilyday.entity.hospital.HospitalReview;
import com.petmilyday.entity.medical.MedicalRecord;
import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.member.PetProfile;
import com.petmilyday.entity.hospital.Hospital;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "reservation")
@Builder
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private PetProfile pet;

    @Column(nullable = false)
    private LocalDate reserveDate;

    @Column(nullable = false)
    private LocalTime reserveTime;

    @Column(nullable = false, length = 50)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.WAITING;
    private Integer waitNumber;

    @Column(length = 255)
    private String cancelReason;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 예약 삭제(병원 삭제로 인한 cascade 포함) 시 진료기록도 함께 삭제되도록 cascade 설정
    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private MedicalRecord medicalRecord;

    // 예약 삭제(병원 삭제로 인한 cascade 포함) 시 이 예약에 달린 리뷰도 함께 삭제되도록 cascade 설정
    // (hospital_review.reservation_id가 not null FK라 Hospital -> Reservation 삭제 순서를 Hibernate가 올바르게 계산하려면 이 매핑이 필요함)
    @Builder.Default
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HospitalReview> reviews = new ArrayList<>();

    public void cancel(String cancelReason){
        this.status = ReservationStatus.CANCEL;
        this.cancelReason = cancelReason;
    }
    //승인 메서드
    public void approve() {
        this.status = ReservationStatus.APPROVED;
    }

    public void adminCancel(){
        this.status = ReservationStatus.CANCEL;
        this.cancelReason ="관리자가 예약 승인을 거부했습니다.";
    }

    public void rejectByHospitalManager(){
        this.status = ReservationStatus.CANCEL;
        this.cancelReason = "병원 관리자가 예약 승인을 거부했습니다.";
    }

    public void done() {
        this.status = ReservationStatus.DONE;
    }

    public void changeWaitNumber(int waitNumber) {
        this.waitNumber = waitNumber;
    }
}