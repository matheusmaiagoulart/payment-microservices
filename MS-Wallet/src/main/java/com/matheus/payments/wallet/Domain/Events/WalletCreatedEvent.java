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
public class WalletCreatedEvent {

    private UUID userId;
    private String cpf;
    private boolean successful;
    private LocalDateTime timestamp;

    public WalletCreatedEvent(UUID userId, String cpf) {
        this.userId = userId;
        this.cpf = cpf;
        this.successful = true;
        this.timestamp = LocalDateTime.now();
    }
}
