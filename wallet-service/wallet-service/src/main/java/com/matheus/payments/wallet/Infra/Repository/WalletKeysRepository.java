package com.matheus.payments.wallet.Infra.Repository;

import com.matheus.payments.wallet.Domain.Wallet.WalletKeys;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletKeysRepository extends JpaRepository<WalletKeys, String> {

    @Query("SELECT wk FROM WalletKeys wk WHERE wk.keyValue = :keyValue")
    Optional<WalletKeys> findwalletIdBykey(String keyValue);

    Boolean existsWalletKeysByKeyValue(String keyValue);
}
