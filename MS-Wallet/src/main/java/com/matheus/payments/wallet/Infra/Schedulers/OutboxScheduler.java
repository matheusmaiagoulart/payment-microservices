package com.matheus.payments.wallet.Infra.Schedulers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.wallet.Domain.Models.Outbox;
import com.matheus.payments.wallet.Infra.Repository.OutboxRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> publisher;

    public OutboxScheduler(OutboxRepository outboxRepository, KafkaTemplate<String, String> publisher) {
        this.outboxRepository = outboxRepository;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelay = 10000)
    public void sendToCreateWallet() {

        List<Outbox> pendingToSend;

        pendingToSend = outboxRepository.findAllBySentFalse();
        pendingToSend.forEach(outbox -> sendOutboxEvent(outbox));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW) // Always create a new transaction for each outbox event
    public void sendOutboxEvent(Outbox outbox) {
        try {

            publisher.send(outbox.getTopic(), outbox.getPayload());
            setOutboxSent(outbox);
            System.out.println("enviadoooo");
        } catch (Exception e) {
            setOutboxFailed(outbox, e.getMessage());
            outboxRepository.save(outbox);
        }
    }

    public void setOutboxSent(Outbox outbox) {
        outbox.setSent(true);
        outboxRepository.save(outbox);
    }

    public void setOutboxFailed(Outbox outbox, String errorMessage) {
        outbox.setFailed(true);
        outbox.setFailureReason(errorMessage);
        outboxRepository.save(outbox);
    }
}
