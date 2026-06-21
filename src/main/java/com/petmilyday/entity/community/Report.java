package com.petmilyday.entity.community;

import com.petmilyday.entity.member.Member;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter; // 신고자

    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType; // "POST" 또는 "COMMENT"

    @Column(name = "target_id", nullable = false)
    private Long targetId; // 대상 글번호 또는 댓글번호

    @Column(nullable = false)
    private String reason; // 신고 사유

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, PROCESSED

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 관리자 액션 메서물 분기
    public void updateStatusToDeleted() {
        this.status = "DELETED";
    }

    public void updateStatusToMaintained() {
        this.status = "MAINTAINED";
    }
}