package com.matheus.payments.wallet.Domain.Events;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * This Event is published after the process of Wallet Creation.
 * It is started by the reception of a UserCreatedEvent from the User Service.
 *
 * @author Matheus Maia Goulart
 */
@Getter @Setter
public class WalletCreationFailed {
    private final UUID userId;
    private final String cpf;
    private final boolean successful;
    private final String errorMessage;
    private final LocalDateTime timestamp;

    public WalletCreationFailed(UUID userId, String cpf, String errorMessage) {
        this.userId = userId;
        this.cpf = cpf;
        this.successful = false;
        this.errorMessage = errorMessage;
        this.timestamp = LocalDateTime.now();
    }
}
