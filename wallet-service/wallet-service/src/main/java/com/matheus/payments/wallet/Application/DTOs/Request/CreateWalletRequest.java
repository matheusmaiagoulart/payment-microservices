package com.matheus.payments.wallet.Application.DTOs.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.shared.Domain.accountType;
import org.shared.Domain.keyType;

import java.util.UUID;

@NoArgsConstructor
@Getter @Setter
public class CreateWalletRequest {


    @JsonProperty("id")
    private UUID accountId;

    @JsonProperty("cpf")
    private String keyValue;

    @JsonProperty("type")
    private keyType keyType;

    @JsonProperty("accountType")
    private accountType accountType;
}
