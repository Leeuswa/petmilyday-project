package com.petmilyday.domain.used;

import com.petmilyday.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "used_post")
public class UsedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String category;

    private int price;

    private String region;

    private Boolean offerAccepted;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_condition")
    private ItemCondition itemCondition;

    @Enumerated(EnumType.STRING)
    private UsedPostStatus status;

    @Column(name = "report_count")
    private Integer reportCount = 0;

    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    // 회원 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 게시글 이미지
    @OneToMany(mappedBy = "usedPost", cascade = CascadeType.ALL)
    private List<UsedPostImg> images;

    // 생성일
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정일
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // memberId 반환용
    public Long getMemberId() {
        return (this.member != null) ? this.member.getId() : null;
    }
}