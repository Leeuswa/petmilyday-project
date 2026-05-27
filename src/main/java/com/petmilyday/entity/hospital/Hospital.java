package com.petmilyday.entity.hospital;

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

    @Column(nullable = false)
    private Boolean isEmergency = false;

    @Column(length = 255)
    private String department;

    @Column(nullable = false)
    private Integer slotIntervalMin = 30;

    @Column(nullable = false)
    private Integer maxPerSlot = 3;

    private Double rating;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "hospital",cascade = CascadeType.ALL)
    private List<HospitalHours> hours = new ArrayList<>();

    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL)
    private List<HospitalImg> images = new ArrayList<>();



}
