package com.matheus.payments.user_service.Domain.Events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.shared.Domain.accountType;
import org.shared.Domain.keyType;

import java.util.UUID;

@NoArgsConstructor
@Getter
public class UserCreatedEvent {

    private UUID id;
    private String cpf;
    private keyType type;
    private accountType accountType;
    private final String topic = "UserCreated";

    public UserCreatedEvent(UUID id, String cpf, accountType accountType) {
        this.id = id;
        this.cpf = cpf;
        this.type = keyType.CPF; // Default PIX key type is CPF when user is created
        this.accountType = accountType;
    }
}
