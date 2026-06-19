package com.petmilyday.dto.usedpost;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MannerScoreDTO {

    private Long usedPostId;

    private Long toMemberId;

    private Integer score;
}
