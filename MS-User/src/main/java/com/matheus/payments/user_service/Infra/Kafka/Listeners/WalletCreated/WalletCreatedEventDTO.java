package com.matheus.payments.user_service.Infra.Kafka.Listeners.WalletCreated;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class WalletCreatedEventDTO{

        private UUID userId;
        private String cpf;
        private boolean successful;
        private LocalDateTime timestamp;
}
