package com.petmilyday.entity.medical;

import com.petmilyday.entity.member.PetProfile;
import com.petmilyday.entity.reservation.Reservation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "medical_record")
@Builder
@AllArgsConstructor
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예약 1건당 진료기록 1건
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    // 진료받은 반려동물
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private PetProfile pet;

    // 진단 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String diagnosis;

    // 치료 내용
    @Column(columnDefinition = "TEXT")
    private String treatment;

    // 처방 내용
    @Column(columnDefinition = "TEXT")
    private String prescription;

    // 예방접종 여부
    @Column(nullable = false)
    private Boolean vaccinated;

    // 백신명
    @Column(length = 100)
    private String vaccineName;

    // 특이사항
    @Column(columnDefinition = "TEXT")
    private String memo;

    // 진단서 PDF URL
    @Column(length = 255)
    private String pdfUrl;

    // 진료일
    @Column(nullable = false)
    private LocalDate visitDate;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void update(String diagnosis, String treatment, String prescription,
                        Boolean vaccinated, String vaccineName, String memo,
                        String pdfUrl, LocalDate visitDate) {
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.prescription = prescription;
        this.vaccinated = vaccinated != null ? vaccinated : false;
        this.vaccineName = vaccineName;
        this.memo = memo;
        this.pdfUrl = pdfUrl;
        this.visitDate = visitDate;
    }
}