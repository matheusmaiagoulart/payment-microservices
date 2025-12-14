package com.matheus.payments.wallet.Infra.Repository;

import com.matheus.payments.wallet.Domain.PixKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PixKeyRepository extends JpaRepository<PixKey, UUID> {

    @Query("SELECT wk FROM PixKey wk WHERE wk.keyValue = :keyValue")
    Optional<PixKey> findAccountIdByKey(String keyValue);

    Boolean existsWalletKeysByKeyValue(String keyValue);
}
