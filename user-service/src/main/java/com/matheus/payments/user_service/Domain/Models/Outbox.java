package com.matheus.payments.user_service.Domain.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "outbox")
public class Outbox {

    @Id
    private UUID id;

    private UUID userId;
    private String eventType;

    @Column(columnDefinition = "json")
    private String payload;
    private boolean isSent;
    private boolean isFailed;
    private String failureReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Outbox(UUID userId, String eventType, String payload) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.eventType = eventType;
        this.payload = payload;
        this.isSent = false;
        this.isFailed = false;
        this.failureReason = null;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }


}
