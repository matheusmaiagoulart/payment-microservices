package com.matheus.payments.UnitTests.Services;

import com.matheus.payments.Application.Audit.OutboxServiceAudit;
import com.matheus.payments.Application.Services.DepositService;
import com.matheus.payments.Application.Services.OutboxService;
import com.matheus.payments.Domain.Exceptions.TransactionNotFound;
import com.matheus.payments.Domain.Models.Deposit;
import com.matheus.payments.Domain.Models.TransactionOutbox;
import com.matheus.payments.Domain.Repositories.OutboxRepository;
import com.matheus.payments.Infra.Exceptions.Custom.DataBaseException;
import com.matheus.payments.UnitTests.Fixtures.OutboxFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxService Tests")
class OutboxServiceTests {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private OutboxServiceAudit audit;

    @Mock
    private DepositService depositService;

    @InjectMocks
    private OutboxService outboxService;

    private TransactionOutbox outbox;
    private String transactionId;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID().toString();
        outbox = OutboxFixture.createOutbox(transactionId, "{\"test\":\"data\"}", "test-topic");
    }

    @Nested
    @DisplayName("SUCCESS SCENARIOS")
    class SuccessScenarios {

        @Test
        @DisplayName("Should get outbox by transaction ID successfully")
        public void shouldGetOutboxByTransactionIdSuccessfully() {
            // Arrange
            when(outboxRepository.findByTransactionId(anyString())).thenReturn(Optional.of(outbox));

            // Act
            TransactionOutbox result = outboxService.getOutboxByTransactionId(transactionId);

            // Assert
            assertNotNull(result);
            assertEquals(transactionId, result.getTransactionId());
            verify(outboxRepository, times(1)).findByTransactionId(transactionId);
        }

        @Test
        @DisplayName("Should save outbox successfully")
        public void shouldSaveOutboxSuccessfully() {
            // Arrange
            doNothing().when(outboxRepository).save(any(TransactionOutbox.class));

            // Act
            outboxService.save(outbox);

            // Assert
            verify(outboxRepository, times(1)).save(outbox);
        }

        @Test
        @DisplayName("Should send outbox entry successfully")
        @SuppressWarnings("unchecked")
        public void shouldSendOutboxEntrySuccessfully() throws Exception {
            // Arrange
            CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(mock(SendResult.class));
            when(kafkaTemplate.send(any(Message.class))).thenReturn(future);
            doNothing().when(outboxRepository).save(any(TransactionOutbox.class));
            doNothing().when(depositService).updateDepositStatus(anyString(), any(Deposit.DepositStatus.class));

            // Act
            outboxService.sendOutboxEntry(outbox);

            // Assert
            verify(kafkaTemplate, times(1)).send(any(Message.class));
            verify(outboxRepository, times(1)).save(outbox);
            verify(depositService, times(1)).updateDepositStatus(transactionId, Deposit.DepositStatus.SENT);
            assertTrue(outbox.getSent());
        }

        @Test
        @DisplayName("Should mark outbox as sent after successful send")
        @SuppressWarnings("unchecked")
        public void shouldMarkOutboxAsSent_AfterSuccessfulSend() throws Exception {
            // Arrange
            CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(mock(SendResult.class));
            when(kafkaTemplate.send(any(Message.class))).thenReturn(future);
            doNothing().when(outboxRepository).save(any(TransactionOutbox.class));
            doNothing().when(depositService).updateDepositStatus(anyString(), any(Deposit.DepositStatus.class));

            // Act
            outboxService.sendOutboxEntry(outbox);

            // Assert
            assertTrue(outbox.getSent());
        }
    }

    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {

        @Test
        @DisplayName("Should fail when outbox does not exist")
        public void shouldFail_WhenOutboxDoesNotExist() {
            // Arrange
            when(outboxRepository.findByTransactionId(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    TransactionNotFound.class,
                    () -> outboxService.getOutboxByTransactionId(transactionId)
            );

            verify(outboxRepository, times(1)).findByTransactionId(transactionId);
        }

        @Test
        @DisplayName("Should fail when save fails")
        public void shouldFail_WhenSaveFails() {
            // Arrange
            doThrow(new DataBaseException("Database error"))
                    .when(outboxRepository).save(any(TransactionOutbox.class));

            // Act & Assert
            DataBaseException exception = assertThrows(
                    DataBaseException.class,
                    () -> outboxService.save(outbox)
            );

            assertTrue(exception.getMessage().contains("An error occurred while saving Outbox Entry"));
            verify(audit, times(1)).logErrorCreateOutbox(anyString(), anyString());
        }

        @Test
        @DisplayName("Should fail when kafka send fails")
        public void shouldFail_WhenKafkaSendFails() {
            // Arrange
            CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Kafka error"));
            when(kafkaTemplate.send(any(Message.class))).thenReturn(future);

            // Act & Assert
            assertThrows(
                    Exception.class,
                    () -> outboxService.sendOutboxEntry(outbox)
            );

            verify(kafkaTemplate, times(1)).send(any(Message.class));
        }
    }
}
