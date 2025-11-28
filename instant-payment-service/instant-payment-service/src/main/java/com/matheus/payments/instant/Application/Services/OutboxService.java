package com.matheus.payments.instant.Application.Services;

import com.matheus.payments.instant.Domain.TransactionOutbox;
import com.matheus.payments.instant.Infra.Repository.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OutboxService {

    private final OutboxRepository outboxRepository;
    public OutboxService(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    public void createOutboxEntry(String transactionId, String payload)
    {
        log.info("Create TransactionOutbox", LogBuilder.serviceLog("/transaction/pix", "Payment",
                transactionId, "OutboxService", "createOutboxEntry", "Payment processing started"));

        // Create Outbox Entry
        TransactionOutbox outbox = new TransactionOutbox(transactionId, payload);
        outboxRepository.save(outbox);
    }
}
