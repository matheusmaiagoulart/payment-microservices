package com.matheus.payments.user_service.Infra.Kafka;

import com.matheus.payments.user_service.Application.Interfaces.UserEventPublisher;
import com.matheus.payments.user_service.Domain.Events.UserCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class UserEventPublisherImpl implements UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    public UserEventPublisherImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publisher(UserCreatedEvent event) {
    kafkaTemplate.send(event.getTopic(), event);
    }
}
