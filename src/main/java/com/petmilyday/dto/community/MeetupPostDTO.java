package com.petmilyday.dto.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetupPostDTO {

    private Long id;

    @NotBlank(message = "제목을 입력해 주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해 주세요.")
    private String content;

    @NotBlank(message = "모임 장소를 입력해 주세요.")
    private String location;

    @NotNull(message = "모임 날짜와 시간을 선택해 주세요.")
    private LocalDateTime meetupDate;

    @Min(value = 2, message = "모임 인원은 최소 2명 이상이어야 합니다.")
    private int maxParticipants;

    // 아래는 서버에서 채워서 화면으로 보낼 때 사용하는 필드들 (작성 시에는 안 받아도 됨)
    private int currentParticipants;
    private String status; // RECRUITING, CLOSED 등
    private String hostName; // 방장 닉네임 또는 이름
    private String hostUsername; // 방장 아이디 (수정/삭제 권한 체크용)
    private int viewCount;
    private LocalDateTime createdAt;

    // 현재 접속한 회원이 이 모임에 참여 중인지 여부 (참여 취소 버튼을 보여주기 위함)
    private boolean isParticipating;
}