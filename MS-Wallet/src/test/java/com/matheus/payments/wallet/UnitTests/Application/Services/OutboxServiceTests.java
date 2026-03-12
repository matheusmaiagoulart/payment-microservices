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

            when(kafkaTemplate.send(any(org.springframework.messaging.Message.class)))
                    .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(null));
            when(outboxRepository.save(any(Outbox.class))).thenReturn(outbox);

            // Act
            outboxService.sendOutboxEvent(outbox);

            // Assert
            verify(kafkaTemplate).send(any(org.springframework.messaging.Message.class));
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

    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {

        @Test
        @DisplayName("Should throw exception when repository fails to save Outbox")
        public void shouldThrowException_WhenRepositoryFailsToSave() {
            // Arrange
            UUID userId = UUID.randomUUID();
            String payload = "{\"userId\": \"" + userId + "\"}";
            String eventType = "CREATE_WALLET";
            String topic = KafkaTopics.WALLET_CREATED_EVENT_TOPIC;
            CorrelationId.set(UUID.randomUUID().toString());

            when(outboxRepository.save(any(Outbox.class)))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // Act & Assert
            assertThrows(RuntimeException.class, () ->
                    outboxService.createOutbox(userId, eventType, topic, payload));

            verify(outboxRepository, times(1)).save(any(Outbox.class));
        }

        @Test
        @DisplayName("Should throw ExecutionException when Kafka send fails")
        public void shouldThrowExecutionException_WhenKafkaSendFails() {
            // Arrange
            Outbox outbox = OutboxFixture.createOutbox();

            java.util.concurrent.CompletableFuture<org.springframework.kafka.support.SendResult<String, String>> failedFuture = new java.util.concurrent.CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("Kafka broker unavailable"));

            when(kafkaTemplate.send(any(org.springframework.messaging.Message.class)))
                    .thenReturn(failedFuture);

            // Act & Assert
            assertThrows(ExecutionException.class, () ->
                    outboxService.sendOutboxEvent(outbox));

            verify(kafkaTemplate, times(1)).send(any(org.springframework.messaging.Message.class));
            verify(outboxRepository, never()).save(any(Outbox.class));
        }

        @Test
        @DisplayName("Should not mark as sent when Kafka throws exception")
        public void shouldNotMarkAsSent_WhenKafkaThrowsException() {
            // Arrange
            Outbox outbox = OutboxFixture.createOutbox();

            when(kafkaTemplate.send(any(org.springframework.messaging.Message.class)))
                    .thenThrow(new RuntimeException("Kafka connection error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () ->
                    outboxService.sendOutboxEvent(outbox));

            assertFalse(outbox.isSent());
            verify(outboxRepository, never()).save(any(Outbox.class));
        }
    }
}
