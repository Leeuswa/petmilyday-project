package com.petmilyday.entity.community;

import com.petmilyday.entity.member.Member;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meetup_participant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MeetupParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetup_post_id", nullable = false)
    private MeetupPost meetupPost; // 모집 게시글

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 참여 회원

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt; // 참여 신청 일시

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @PrePersist
    public void prePersist() {
        this.joinedAt = LocalDateTime.now();
    }

    public void approve() {
        this.status = "APPROVED";
    }
}