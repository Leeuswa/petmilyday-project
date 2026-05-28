package com.petmilyday.dto.usedpost;

import com.petmilyday.entity.used.*;
import com.petmilyday.entity.used.ItemCondition;
import com.petmilyday.entity.used.UsedPost;
import com.petmilyday.entity.used.UsedPostStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsedPostDTO {

    private Long id;
    private Long memberId;

    private String title;
    private String content;
    private String category;

    private int price;

    private String region;

    private ItemCondition itemCondition;

    private UsedPostStatus status;

    private Boolean isHidden;

    private Integer reportCount;

    // 가격제안가능
    private Boolean offerAccepted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<String> imageUrlList;

    private String writerName;

    public UsedPostDTO(UsedPost post) {

        this.id = post.getId();

        if (post.getMember() != null) {

            this.memberId = post.getMember().getId();

            this.writerName = post.getMember().getNickname();

        } else {

            this.writerName = "관리자";
        }

        this.title = post.getTitle();
        this.content = post.getContent();
        this.category = post.getCategory();
        this.price = post.getPrice();
        this.region = post.getRegion();

        this.offerAccepted =
                post.getOfferAccepted() != null
                        ? post.getOfferAccepted()
                        : false;

        this.itemCondition = post.getItemCondition();

        // enum 기본값
        this.status =
                post.getStatus() != null
                        ? post.getStatus()
                        : UsedPostStatus.SELLING;

        this.isHidden =
                post.getIsHidden() != null
                        ? post.getIsHidden()
                        : false;

        this.reportCount =
                post.getReportCount() != null
                        ? post.getReportCount()
                        : 0;

        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();

        if (post.getImages() != null) {

            this.imageUrlList =
                    post.getImages()
                            .stream()
                            .map(UsedPostImg::getImgUrl)
                            .collect(Collectors.toList());
        }
    }

    public UsedPost toEntity() {

        return UsedPost.builder()
                .title(this.title)
                .content(this.content)
                .itemCondition(this.itemCondition)
                .price(this.price)
                .category(this.category)
                .region(this.region)

                // 가격제안가능
                .offerAccepted(
                        this.offerAccepted != null
                                ? this.offerAccepted
                                : false
                )

                // enum 기본값
                .status(
                        this.status != null
                                ? this.status
                                : UsedPostStatus.SELLING
                )

                .isHidden(
                        this.isHidden != null
                                ? this.isHidden
                                : false
                )

                .reportCount(
                        this.reportCount != null
                                ? this.reportCount
                                : 0
                )

                .build();
    }

    // 상태 한글 변환
    public String getStatusLabel() {

        if (status == null) {
            return "판매중";
        }

        return switch (status) {

            case SELLING -> "판매중";

            case RESERVED -> "예약중";

            case SOLD -> "판매완료";
        };
    }
}