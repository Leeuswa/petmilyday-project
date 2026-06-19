package com.petmilyday.entity.community;

import com.petmilyday.entity.member.Member;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meetup_post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MeetupPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private Member host;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(name = "meetup_date", nullable = false)
    private LocalDateTime meetupDate;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants; // 모집 최대 인원

    @Column(name = "current_participants", nullable = false)
    @Builder.Default
    private int currentParticipants = 1; // 방장 포함 1명 시작

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MeetupStatus status = MeetupStatus.RECRUITING;

    @Column(nullable = false)
    @Builder.Default
    private int viewCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 모임 정보 수정 및 인원 상태 체크
    public void updateMeetup(String title, String content, String location, LocalDateTime meetupDate, int maxParticipants) {
        this.title = title;
        this.content = content;
        this.location = location;
        this.meetupDate = meetupDate;
        this.maxParticipants = maxParticipants;

        // 정원 충족 여부에 따른 상태 자동 전환
        if (this.currentParticipants >= maxParticipants) {
            this.status = MeetupStatus.CLOSED;
        } else if (this.status == MeetupStatus.CLOSED && this.currentParticipants < maxParticipants) {
            this.status = MeetupStatus.RECRUITING;
        }
    }

    // 인원 증가 및 변경 로직
    public void addParticipant() {
        if (this.currentParticipants >= this.maxParticipants) {
            throw new IllegalStateException("이미 모집 인원이 꽉 찼습니다.");
        }
        this.currentParticipants++;
        if (this.currentParticipants == this.maxParticipants) {
            this.status = MeetupStatus.CLOSED; // 꽉 차면 자동 마감
        }
    }

    // 인원 감소 및 상태 롤백 로직
    public void removeParticipant() {
        this.currentParticipants--;
        // 마감 상태 중 취소자가 생기면 모집 중으로 변경
        if (this.status == MeetupStatus.CLOSED && this.currentParticipants < this.maxParticipants) {
            this.status = MeetupStatus.RECRUITING;
        }
    }

    public void addViewCount() {
        this.viewCount++;
    }
}