package com.petmilyday.dto.hospital;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class HospitalRequestDTO {

    private String keyword; // 병원명
    private Boolean isEmergency; // 응급 필터
    private String department; // 진료와 필터
    private String region; // 근처 병원 검색용 지역/주소 입력값
    private Double latitude; //현재 위치 위도
    private Double longitude; //현재 위치 경도
}
