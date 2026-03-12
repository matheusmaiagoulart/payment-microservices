package com.matheus.payments.wallet.Domain.Repositories;

import com.matheus.payments.wallet.Domain.Models.PixKey;

import java.util.Optional;

public interface PixKeyRepository {

    Optional<PixKey> findAccountIdByKey(String keyValue);

    PixKey saveAndFlush(PixKey pixKey);
}

