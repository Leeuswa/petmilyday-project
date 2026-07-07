package com.petmilyday.entity.diagnosis;

import com.petmilyday.entity.member.Member;
import com.petmilyday.entity.member.PetProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "diagnosis_history")
public class DiagnosisHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 반려동물
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private PetProfile pet;

    @Column(columnDefinition = "TEXT")
    private String symptomText;

    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String resultDisease;

    private String resultSeverity;

    @Column(columnDefinition = "TEXT")
    private String resultRecommend;

    private LocalDateTime createdAt;
}