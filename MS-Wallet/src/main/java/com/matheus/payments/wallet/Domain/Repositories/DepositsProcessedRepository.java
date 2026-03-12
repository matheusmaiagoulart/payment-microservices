package com.matheus.payments.wallet.Domain.Repositories;

import com.matheus.payments.wallet.Domain.Models.DepositsProcessed;

public interface DepositsProcessedRepository {

    DepositsProcessed saveAndFlush(DepositsProcessed depositsProcessed);
}

