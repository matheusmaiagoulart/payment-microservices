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
    private String topic;

    @Column(columnDefinition = "json")
    private String payload;
    private UUID correlationId;
    private boolean isSent;
    private boolean isFailed;
    private String failureReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Outbox(UUID userId, String eventType, String topic, String payload, String correlationId) {
        this.id = UUID.randomUUID();
        this.correlationId = UUID.fromString(correlationId);
        this.userId = userId;
        this.eventType = eventType;
        this.topic = topic;
        this.payload = payload;
        this.isSent = false;
        this.isFailed = false;
        this.failureReason = null;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }


}
