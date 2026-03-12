package com.matheus.payments.wallet.Infra.Schedulers;


import com.matheus.payments.wallet.Application.Services.OutboxService;
import com.matheus.payments.wallet.Domain.Models.Outbox;
import com.matheus.payments.wallet.Domain.Repositories.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * This Scheduler is responsible for get all pending Outbox events and send them to respective Kafka Topics.
 *
 * @author Matheus Maia Goulart
 */
@Slf4j
@Service
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final OutboxService outboxService;

    public OutboxScheduler(OutboxService outboxService, OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
        this.outboxService = outboxService;
    }

    @Scheduled(fixedDelay = 10000)
    public void sendToCreateWallet() {
        List<Outbox> pendingToSend = outboxRepository.findAllBySentFalse();
        pendingToSend.forEach(outbox -> {
            try {
                outboxService.sendOutboxEvent(outbox);
            } catch (ExecutionException | InterruptedException e) {
                log.error("Failed to process outbox. Outbox Id: {} ", outbox.getId());
            }
        });
    }
}
