package com.petmilyday.domain.chat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roomId;
    private Long senderId;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime createdAt;

    @Column(name = "is_read")
    private Boolean isRead = false;
}