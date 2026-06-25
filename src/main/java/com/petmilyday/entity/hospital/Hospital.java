package com.petmilyday.entity.hospital;

import com.petmilyday.entity.reservation.Reservation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "hospital")
@Builder
@AllArgsConstructor
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, precision = 10 , scale = 7)
    private BigDecimal latitude;

    //precision = 전체 10자리
    //scale = 소수점 이하 7자리
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(nullable = false, length = 20)
    private String phone;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isEmergency = false;

    @Builder.Default
    @Column(length = 255)
    private String department = "미입력";

    @Builder.Default
    @Column(nullable = false)
    private Integer slotIntervalMin = 30;

    @Builder.Default
    @Column(nullable = false)
    private Integer maxPerSlot = 3;

    private Double rating;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HospitalHours> hours = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL)
    private List<HospitalImg> images = new ArrayList<>();

    // 병원 삭제 시 해당 병원의 예약/리뷰/병원관리자 기록도 함께 삭제되도록 cascade 설정
    @Builder.Default
    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HospitalReview> reviews = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HospitalManager> managers = new ArrayList<>();

    // 메인 관리자: 병원 기본정보 수정
    public void updateBasicInfo(String name,
                                String address,
                                BigDecimal latitude,
                                BigDecimal longitude,
                                String phone) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
    }

    // 병원 관리자: 병원 세부정보 수정
    public void updateDetailInfo(Boolean isEmergency,
                                 String department,
                                 Integer slotIntervalMin,
                                 Integer maxPerSlot) {
        this.isEmergency = isEmergency;
        this.department = department;
        this.slotIntervalMin = slotIntervalMin;
        this.maxPerSlot = maxPerSlot;
    }

}
