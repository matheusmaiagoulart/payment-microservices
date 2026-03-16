package com.matheus.payments.user_service.Infra.Schedulers;

import com.matheus.payments.user_service.Application.Services.OutboxService;
import com.matheus.payments.user_service.Domain.Models.Outbox;
import com.matheus.payments.user_service.Domain.Repositories.OutboxRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This scheduler is responsible for sending pending outbox events to Kafka.
 * It runs at a fixed delay and processes all outbox events that have not been sent yet.
 *
 * @author Matheus Maia Goulart
 */
@Service
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final OutboxService outboxService;

    public OutboxScheduler(OutboxRepository outboxRepository, OutboxService outboxService) {
        this.outboxService = outboxService;
        this.outboxRepository = outboxRepository;
    }

    @Scheduled(fixedDelay = 6000)
    public void sendToCreateWallet() {
        List<Outbox> pendingToSend = outboxRepository.findAllBySentFalse();
        pendingToSend.forEach(outbox -> outboxService.sendOutboxEvent(outbox));
    }
}
