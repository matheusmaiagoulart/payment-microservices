package com.matheus.payments.wallet.Infra.Repository.JpaImplements;

import com.matheus.payments.wallet.Domain.Models.WalletLedger;
import com.matheus.payments.wallet.Domain.Repositories.WalletLedgerRepository;
import com.matheus.payments.wallet.Infra.Repository.JpaInterfaces.JpaWalletLedgerRepository;
import org.springframework.stereotype.Repository;

@Repository
public class WalletLedgerRepositoryImpl implements WalletLedgerRepository {

    private final JpaWalletLedgerRepository jpaWalletLedgerRepository;

    public WalletLedgerRepositoryImpl(JpaWalletLedgerRepository jpaWalletLedgerRepository) {
        this.jpaWalletLedgerRepository = jpaWalletLedgerRepository;
    }

    @Override
    public WalletLedger saveAndFlush(WalletLedger walletLedger) {
        return jpaWalletLedgerRepository.saveAndFlush(walletLedger);
    }
}


