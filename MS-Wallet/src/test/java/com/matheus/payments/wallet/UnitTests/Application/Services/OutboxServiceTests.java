package com.matheus.payments.wallet.UnitTests.Application.Services;

import com.matheus.payments.wallet.Application.Audit.CorrelationId;
import com.matheus.payments.wallet.Application.Services.OutboxService;
import com.matheus.payments.wallet.Domain.Models.Outbox;
import com.matheus.payments.wallet.Domain.Repositories.OutboxRepository;
import com.matheus.payments.wallet.UnitTests.Fixtures.OutboxFixture;
import com.matheus.payments.wallet.utils.KafkaTopics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OutboxServiceTests {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @Mock
    private OutboxRepository outboxRepository;

    @InjectMocks
    private OutboxService outboxService;

    @Nested
    @DisplayName("SUCCESS SCENARIOS")
    class SuccessScenarios {

        @Test
        @DisplayName("Should create Outbox Entry successfully")
        public void shouldCreateOutboxEntrySuccessfully() {
            // Arrange
            UUID userId = UUID.randomUUID();
            String payload = "{\"userId\": \"" + userId + "\", \"action\": \"CREATE_WALLET\"}";
            String eventType = "CREATE_WALLET";
            String topic = KafkaTopics.WALLET_CREATED_EVENT_TOPIC;
            CorrelationId.set(UUID.randomUUID().toString());

            when(outboxRepository.save(any(Outbox.class))).thenReturn(any(Outbox.class));

            // Act
            outboxService.createOutbox(userId, eventType, topic, payload);

            // Assert
            verify(outboxRepository).save(any(Outbox.class));
        }

        @Test
        @DisplayName("Should send event to Kafka and mark Outbox as sent")
        public void shouldSendEventToKafkaAndMarkOutboxAsSent() throws ExecutionException, InterruptedException {
            ArgumentCaptor<Outbox> captor = ArgumentCaptor.forClass(Outbox.class);

            // Arrange
            Outbox outbox = OutboxFixture.createOutbox();

            when(kafkaTemplate.send(outbox.getTopic(), outbox.getPayload())).thenReturn(null);
            when(outboxRepository.save(any(Outbox.class))).thenReturn(outbox);

            // Act
            outboxService.sendOutboxEvent(outbox);

            // Assert
            verify(kafkaTemplate).send(outbox.getTopic(), outbox.getPayload());
            verify(outboxRepository).save(captor.capture());
            Outbox capturedOutbox = captor.getValue();

            assertTrue(capturedOutbox.isSent());
            assertFalse(capturedOutbox.isFailed());
            assertEquals(outbox.getId(), capturedOutbox.getId());
            assertEquals(outbox.getPayload(), capturedOutbox.getPayload());
            assertEquals(outbox.getTopic(), capturedOutbox.getTopic());
            assertEquals(outbox.getEventType(), capturedOutbox.getEventType());
            assertEquals(outbox.getUserId(), capturedOutbox.getUserId());
        }
    }
}
