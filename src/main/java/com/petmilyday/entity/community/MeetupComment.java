package com.petmilyday.entity.community;

import com.petmilyday.entity.member.Member;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meetup_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MeetupComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 🌟 일반 게시글이 아닌 '모임 게시글'과 연결!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetup_post_id", nullable = false)
    private MeetupPost meetupPost;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private MeetupComment parent;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<MeetupCommentLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<MeetupComment> children = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void changeContent(String content) {
        this.content = content;
    }
}