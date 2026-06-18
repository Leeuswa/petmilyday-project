package com.petmilyday.dto.notification;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationDTO {

    private String type;
    private String message;
    private String url;
    private LocalDateTime createdAt;

    private Long reservationId;
    private Integer waitNumber;
}