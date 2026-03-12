package com.matheus.payments.wallet.Domain.Repositories;

import com.matheus.payments.wallet.Domain.Models.Outbox;

import java.util.List;

public interface OutboxRepository {

    List<Outbox> findAllBySentFalse();

    Outbox save(Outbox outbox);
}

