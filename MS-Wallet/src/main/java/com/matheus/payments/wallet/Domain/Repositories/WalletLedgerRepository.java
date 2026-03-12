package com.matheus.payments.wallet.Domain.Repositories;

import com.matheus.payments.wallet.Domain.Models.WalletLedger;

public interface WalletLedgerRepository {

    WalletLedger saveAndFlush(WalletLedger walletLedger);
}

