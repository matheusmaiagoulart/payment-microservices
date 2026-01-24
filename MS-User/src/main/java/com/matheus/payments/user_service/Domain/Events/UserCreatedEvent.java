package com.matheus.payments.user_service.Domain.Events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.shared.Domain.accountType;
import org.shared.Domain.keyType;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
public class UserCreatedEvent {

    private UUID userId;
    private String cpf;
    private keyType type;
    private accountType accountType;
    private LocalDateTime timestamp;

    public UserCreatedEvent(UUID userId, String cpf, accountType accountType) {
        this.userId = userId;
        this.cpf = cpf;
        this.type = keyType.CPF; // Default PIX key type is CPF when user is created
        this.accountType = accountType;
        this.timestamp = LocalDateTime.now();
    }
}
