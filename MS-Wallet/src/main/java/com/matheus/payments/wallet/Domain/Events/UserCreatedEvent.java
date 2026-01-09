package com.matheus.payments.wallet.Domain.Events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.shared.Domain.accountType;
import org.shared.Domain.keyType;

import java.util.UUID;

/**
 * This event is published when a new user is created in the User Service.
 * It contains the necessary information to create a Wallet for the user.
 *
 * @author Matheus Maia Goulart
 */
@NoArgsConstructor
@Getter @Setter
public class UserCreatedEvent {


    @JsonProperty("id")
    private UUID accountId;

    @JsonProperty("cpf")
    private String keyValue;

    @JsonProperty("type")
    private keyType keyType;

    @JsonProperty("accountType")
    private accountType accountType;
}
