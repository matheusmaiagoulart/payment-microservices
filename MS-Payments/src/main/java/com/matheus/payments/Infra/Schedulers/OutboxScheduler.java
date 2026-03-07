package com.matheus.payments.Infra.Schedulers;

import com.matheus.payments.Application.Audit.OutboxServiceAudit;
import com.matheus.payments.Application.Services.OutboxService;
import com.matheus.payments.Domain.Repositories.OutboxRepository;
import com.matheus.payments.Infra.Exceptions.Custom.DataBaseException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final OutboxServiceAudit audit;
    private final OutboxService outboxService;

    public OutboxScheduler(OutboxRepository outboxRepository, OutboxService outboxService, OutboxServiceAudit audit) {
        this.audit = audit;
        this.outboxRepository = outboxRepository;
        this.outboxService = outboxService;
    }

    @Scheduled(fixedDelay = 10000)
    public void processPendingOutbox() {
        var pendingOutbox = outboxRepository.findBySentFalseOrderByCreatedAtAsc();

        pendingOutbox.forEach(outbox -> {
            try {
                outboxService.sendOutboxEntry(outbox);
            } catch (Exception e) {
                audit.logErrorCreateOutbox(outbox.getTransactionId(), e.getMessage());
                throw new DataBaseException("An error occurred while sending Outbox Entry for Outbox Id: " + outbox.getTransactionId());
            }
        });

    }
}
