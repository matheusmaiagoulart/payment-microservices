
package com.matheus.payments.user_service.Domain.Repositories;

import com.matheus.payments.user_service.Domain.Models.Outbox;

import java.util.List;

public interface OutboxRepository {

    Outbox save(Outbox outbox);

    List<Outbox> findAllBySentFalse();
}

