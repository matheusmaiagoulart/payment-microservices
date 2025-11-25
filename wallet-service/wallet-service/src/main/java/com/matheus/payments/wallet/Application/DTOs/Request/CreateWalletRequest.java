package com.matheus.payments.wallet.Application.DTOs.Request;

import com.matheus.payments.wallet.Domain.Wallet.accountType;
import com.matheus.payments.wallet.Domain.Wallet.keyType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Getter @Setter
public class CreateWalletRequest {

    @NotNull
    @NotEmpty
    public String keyValue;

    @NotNull
    @NotEmpty
    public keyType keyType;

    @NotNull
    @NotEmpty
    @Enumerated(EnumType.STRING)
    public accountType accountType;
}
