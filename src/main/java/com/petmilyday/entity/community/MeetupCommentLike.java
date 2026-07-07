package com.petmilyday.entity.community;

import com.petmilyday.entity.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "meetup_comment_likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MeetupCommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetup_comment_id", nullable = false)
    private MeetupComment comment;
}