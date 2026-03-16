package com.matheus.payments.user_service.Application.Services;

import com.matheus.payments.user_service.Application.Audit.CorrelationId;
import com.matheus.payments.user_service.Application.Audit.OutboxServiceAudit;
import com.matheus.payments.user_service.Domain.Models.Outbox;
import com.matheus.payments.user_service.Domain.Repositories.OutboxRepository;
import com.matheus.payments.user_service.Fixtures.OutboxFixture;
import com.matheus.payments.user_service.Infra.Exceptions.Custom.ErrorToSaveOutboxException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxService Tests")
class OutboxServiceTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private OutboxServiceAudit audit;

    @Mock
    private KafkaTemplate<String, String> publisher;

    @InjectMocks
    private OutboxService outboxService;

    private static final String CORRELATION_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Test
    @DisplayName("Should create outbox successfully")
    void shouldCreateOutboxSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String eventType = "UserCreated";
        String topic = "user-created-topic";
        String payload = "{\"userId\":\"123\"}";

        try (MockedStatic<CorrelationId> mockedCorrelationId = mockStatic(CorrelationId.class)) {
            mockedCorrelationId.when(CorrelationId::get).thenReturn(CORRELATION_ID);
            when(outboxRepository.save(any(Outbox.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            outboxService.createOutbox(userId, eventType, topic, payload);

            // Assert
            verify(outboxRepository, times(1)).save(any(Outbox.class));
            verify(audit, times(1)).logStartOutboxCreate(userId);
            verify(audit, times(1)).logOutboxCreatedSuccessfully();
        }
    }

    @Test
    @DisplayName("Should send outbox event successfully")
    void shouldSendOutboxEventSuccessfully() {
        // Arrange
        Outbox outbox = OutboxFixture.createValidOutbox();
        assertFalse(outbox.isSent());

        when(outboxRepository.save(any(Outbox.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        outboxService.sendOutboxEvent(outbox);

        // Assert
        assertTrue(outbox.isSent());
        verify(publisher, times(1)).send(any(Message.class));
        verify(outboxRepository, times(1)).save(outbox);
    }

    @Test
    @DisplayName("Should mark outbox as failed when send fails")
    void shouldMarkOutboxAsFailedWhenSendFails() {
        // Arrange
        Outbox outbox = OutboxFixture.createValidOutbox();
        String errorMessage = "Connection refused";

        when(publisher.send(any(Message.class))).thenThrow(new RuntimeException(errorMessage));
        when(outboxRepository.save(any(Outbox.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        outboxService.sendOutboxEvent(outbox);

        // Assert
        assertTrue(outbox.isFailed());
        assertEquals(errorMessage, outbox.getFailureReason());
        verify(outboxRepository, times(2)).save(outbox); // setOutboxFailed saves + sendOutboxEvent saves
    }

    @Test
    @DisplayName("Should throw ErrorToSaveOutboxException in fallback method")
    void shouldThrowErrorToSaveOutboxExceptionInFallbackMethod() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String eventType = "UserCreated";
        String topic = "user-created-topic";
        String payload = "{\"userId\":\"123\"}";
        Throwable throwable = new RuntimeException("Database error");

        // Act & Assert
        assertThrows(ErrorToSaveOutboxException.class, () -> {
            outboxService.handleErrorToSaveOutboxEvent(userId, eventType, topic, payload, throwable);
        });
    }
}




