package com.matheus.payments.wallet.Application.Services;

import com.matheus.payments.wallet.Domain.Models.Outbox;
import com.matheus.payments.wallet.Infra.Repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OutboxService {

    private final OutboxRepository outboxRepository;

    public OutboxService(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createOutbox(UUID userId, String eventType, String topic, String payload) {
        Outbox outbox = new Outbox(userId, eventType, topic, payload);
        outboxRepository.save(outbox);
    }
}
