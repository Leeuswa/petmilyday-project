package com.petmilyday.entity.used;

import com.petmilyday.entity.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MannerScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 평가한 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_member_id", nullable = false)
    private Member fromMember;

    // 평가받은 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_member_id", nullable = false)
    private Member toMember;

    // 거래 게시글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_post_id", nullable = false)
    private UsedPost usedPost;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}