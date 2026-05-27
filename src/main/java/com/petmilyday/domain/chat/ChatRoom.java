package com.petmilyday.domain.chat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;
    private Long buyerId;
    private Long sellerId;

    private LocalDateTime createdAt;
}