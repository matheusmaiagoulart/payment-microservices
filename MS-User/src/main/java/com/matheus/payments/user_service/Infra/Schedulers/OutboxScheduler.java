package com.matheus.payments.user_service.Infra.Schedulers;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.matheus.payments.user_service.Domain.Models.Outbox;
import com.matheus.payments.user_service.Infra.Repository.OutboxRepository;
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
    private final KafkaTemplate<String, String> publisher;

    public OutboxScheduler(OutboxRepository outboxRepository, KafkaTemplate<String, String> publisher) {
        this.outboxRepository = outboxRepository;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelay = 6000)
    public void sendToCreateWallet() {
        List<Outbox> pendingToSend = outboxRepository.findAllBySentFalse();
        pendingToSend.forEach(outbox -> sendOutboxEvent(outbox));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW) // Always create a new transaction for each outbox event
    public void sendOutboxEvent(Outbox outbox) {
        try {
            Message<String> message = MessageBuilder
                    .withPayload(outbox.getPayload())
                    .setHeader("correlationId", outbox.getCorrelationId().toString())
                    .setHeader(KafkaHeaders.TOPIC, outbox.getTopic())
                    .build();

            publisher.send(message);
            setOutboxSent(outbox);
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
