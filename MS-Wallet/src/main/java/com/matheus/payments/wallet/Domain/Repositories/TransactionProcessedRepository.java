package com.matheus.payments.wallet.Domain.Repositories;

import com.matheus.payments.wallet.Domain.Models.TransactionsProcessed;

public interface TransactionProcessedRepository {

    TransactionsProcessed saveAndFlush(TransactionsProcessed transactionsProcessed);
}

