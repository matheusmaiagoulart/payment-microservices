package com.matheus.payments.wallet.Domain.Repositories;

import com.matheus.payments.wallet.Domain.Models.Wallet;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {

    Optional<Wallet> findByAccountIdAndIsActiveTrue(UUID userId);

    Boolean existsBySocialId(String socialId);

    Wallet saveAndFlush(Wallet wallet);
}

