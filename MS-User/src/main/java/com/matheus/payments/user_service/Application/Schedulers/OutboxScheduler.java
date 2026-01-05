package com.matheus.payments.user_service.Application.Schedulers;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import com.matheus.payments.user_service.Application.Interfaces.UserEventPublisher;
import com.matheus.payments.user_service.Domain.Events.UserCreatedEvent;
import com.matheus.payments.user_service.Domain.Models.Outbox;
import com.matheus.payments.user_service.Infra.Repository.OutboxRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final UserEventPublisher publisher;
    private final ObjectMapper objectMapper;

    public OutboxScheduler(ObjectMapper mapper, OutboxRepository outboxRepository, UserEventPublisher publisher) {
        this.outboxRepository = outboxRepository;
        this.publisher = publisher;
        this.objectMapper = mapper;
    }

    @Scheduled(fixedDelay = 6000)
    public void sendToCreateWallet() {

        List<Outbox> pendingToSend;

        pendingToSend = outboxRepository.findAllBySentFalse();
        pendingToSend.forEach(outbox -> sendOutboxEvent(outbox));
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW) // Always create a new transaction for each outbox event
    public void sendOutboxEvent(Outbox outbox) {
        try {
            UserCreatedEvent userCreatedEvent = objectMapper.readValue(outbox.getPayload(), UserCreatedEvent.class);
            publisher.publisher(userCreatedEvent);
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
