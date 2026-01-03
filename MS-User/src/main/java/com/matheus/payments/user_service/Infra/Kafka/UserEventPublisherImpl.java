package com.matheus.payments.user_service.Infra.Kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.user_service.Application.Interfaces.UserEventPublisher;
import com.matheus.payments.user_service.Domain.Events.UserCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class UserEventPublisherImpl implements UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public UserEventPublisherImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publisher(UserCreatedEvent event) throws JsonProcessingException {

        String jsonPayload = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(event.getTopic(), jsonPayload);
    }
}
