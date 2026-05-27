package com.petmilyday.domain.member;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
public class MannerScore {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) // 이 부분이 꼭 있어야 합니다!
    private Long id;

    @Column(nullable = false) private Long memberId;
    @Column(nullable = false) private Double score;

    private String feedback;
}