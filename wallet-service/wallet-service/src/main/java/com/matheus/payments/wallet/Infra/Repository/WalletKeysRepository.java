package com.matheus.payments.wallet.Infra.Repository;

import com.matheus.payments.wallet.Domain.WalletKeys;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletKeysRepository extends JpaRepository<WalletKeys, UUID> {

    @Query("SELECT wk FROM WalletKeys wk WHERE wk.keyValue = :keyValue")
    Optional<WalletKeys> findAccountIdByKey(String keyValue);

    Boolean existsWalletKeysByKeyValue(String keyValue);
}
