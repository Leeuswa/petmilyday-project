package com.petmilyday.entity.hospital;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "hospital_hours")
public class HospitalHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Column(nullable = false)
    private Integer dayOfWeek; // 0=월 1=화 2=수 3=목 4=금 5=토 6=일

    private LocalTime openTime;

    private LocalTime closeTime;

    @Column(nullable = false)
    private Boolean isClosed = false;
}