package com.petmilyday.dto.community;

import lombok.*;
import java.time.LocalDateTime;

public class ReportDTO {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String targetType; // "POST" or "COMMENT"
        private Long targetId;
        private String reason;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Long id;
        private String reporterUsername;
        private String targetType;
        private Long targetId;
        private String reason;
        private String status;
        private LocalDateTime createdAt;
        private String targetContentSummary; // 화면 표시용 본문 요약
    }
}