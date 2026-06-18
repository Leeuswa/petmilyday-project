package com.petmilyday.dto.usedpost;

import com.petmilyday.entity.used.ItemCondition;
import com.petmilyday.entity.used.UsedPost;
import com.petmilyday.entity.used.UsedPostImg;
import com.petmilyday.entity.used.UsedPostStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Min(0)
    @Max(1000000000)
    private Integer price;

    private String region;

    private ItemCondition itemCondition;

    private UsedPostStatus status;

    private Boolean isHidden;

    private Integer reportCount;

    // 가격제안 가능 여부
    private Boolean offerAccepted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<String> imageUrlList;

    private String writerName;

    private Double mannerAverage;

    // Entity -> DTO
    public UsedPostDTO(UsedPost post) {

        this.id = post.getId();

        // 작성자
        if (post.getMember() != null) {

            this.memberId = post.getMember().getId();

            if (post.getMember().getNickname() != null
                    && !post.getMember().getNickname().isBlank()) {

                this.writerName =
                        post.getMember().getNickname();

            } else {

                this.writerName =
                        post.getMember().getUsername();
            }

        } else {

            this.writerName = "알수없음";
        }

        this.title = post.getTitle();

        this.content = post.getContent();

        this.category = post.getCategory();

        this.price = post.getPrice();

        this.region = post.getRegion();

        this.itemCondition = post.getItemCondition();

        // 상태 기본값
        this.status =
                post.getStatus() != null
                        ? post.getStatus()
                        : UsedPostStatus.SELLING;

        // 숨김 기본값
        this.isHidden =
                post.getIsHidden() != null
                        ? post.getIsHidden()
                        : false;

        // 신고수 기본값
        this.reportCount =
                post.getReportCount() != null
                        ? post.getReportCount()
                        : 0;

        // 가격제안 기본값
        this.offerAccepted =
                post.getOfferAccepted() != null
                        ? post.getOfferAccepted()
                        : false;

        // 매너점수 평균
        this.mannerAverage =
                post.getMannerAverage() != null
                        ? post.getMannerAverage()
                        : 0.0;

        this.createdAt = post.getCreatedAt();

        this.updatedAt = post.getUpdatedAt();

        // 이미지
        if (post.getImages() != null) {

            this.imageUrlList =
                    post.getImages()
                            .stream()
                            .map(UsedPostImg::getImgUrl)
                            .collect(Collectors.toList());
        }
    }

    // DTO -> Entity
    public UsedPost toEntity() {

        return UsedPost.builder()

                .title(this.title)

                .content(this.content)

                .category(this.category)

                .price(this.price)

                .region(this.region)

                .itemCondition(this.itemCondition)

                // 가격제안 가능
                .offerAccepted(
                        this.offerAccepted != null
                                ? this.offerAccepted
                                : false
                )

                // 상태 기본값
                .status(
                        this.status != null
                                ? this.status
                                : UsedPostStatus.SELLING
                )

                // 숨김 기본값
                .isHidden(
                        this.isHidden != null
                                ? this.isHidden
                                : false
                )

                // 신고수 기본값
                .reportCount(
                        this.reportCount != null
                                ? this.reportCount
                                : 0
                )

                // 매너점수 기본값
                .mannerAverage(
                        this.mannerAverage != null
                                ? this.mannerAverage
                                : 0.0
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