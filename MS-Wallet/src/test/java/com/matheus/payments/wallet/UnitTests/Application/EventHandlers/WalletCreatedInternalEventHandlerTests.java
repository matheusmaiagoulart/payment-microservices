package com.matheus.payments.wallet.UnitTests.Application.EventHandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.wallet.Application.EventHandlers.WalletHandlers.WalletCreatedInternalEventHandler;
import com.matheus.payments.wallet.Application.Events.CreateWallet.WalletCreatedEvent;
import com.matheus.payments.wallet.Application.Services.OutboxService;
import com.matheus.payments.wallet.UnitTests.Fixtures.WalletCreatedEventFixture;
import com.matheus.payments.wallet.utils.KafkaTopics;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletCreatedInternalEventHandlerTests {

    @Mock
    private OutboxService outboxService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WalletCreatedInternalEventHandler eventHandler;

    private WalletCreatedEvent createValidEvent() {
        return WalletCreatedEventFixture.createWalletCreatedEvent(UUID.randomUUID(), "11111111111");
    }

    @Nested
    @DisplayName("SUCCESS SCENARIOS")
    class SuccessScenarios {

        @Test
        @DisplayName("Should handle wallet created event successfully")
        public void shouldHandleWalletCreatedEventSuccessfully() throws JsonProcessingException {
            // Arrange
            WalletCreatedEvent event = createValidEvent();
            String eventJson = "{\"userId\":\"" + event.getUserId() + "\",\"cpf\":\"" + event.getCpf() + "\"}";

            when(objectMapper.writeValueAsString(event)).thenReturn(eventJson);
            doNothing().when(outboxService).createOutbox(
                    eq(event.getUserId()),
                    eq("WalletCreated"),
                    eq(KafkaTopics.WALLET_CREATED_EVENT_TOPIC),
                    eq(eventJson)
            );

            // Act
            assertDoesNotThrow(() -> eventHandler.handler(event));

            // Assert
            verify(objectMapper, times(1)).writeValueAsString(event);
            verify(outboxService, times(1)).createOutbox(
                    event.getUserId(),
                    "WalletCreated",
                    KafkaTopics.WALLET_CREATED_EVENT_TOPIC,
                    eventJson
            );
        }

        @Test
        @DisplayName("Should serialize event correctly")
        public void shouldSerializeEventCorrectly() throws JsonProcessingException {
            // Arrange
            WalletCreatedEvent event = createValidEvent();
            String expectedJson = "{\"userId\":\"test\",\"cpf\":\"11111111111\"}";

            when(objectMapper.writeValueAsString(event)).thenReturn(expectedJson);
            doNothing().when(outboxService).createOutbox(any(), anyString(), anyString(), anyString());

            // Act
            assertDoesNotThrow(() -> eventHandler.handler(event));

            // Assert
            verify(objectMapper, times(1)).writeValueAsString(event);
            verify(outboxService, times(1)).createOutbox(
                    any(UUID.class),
                    eq("WalletCreated"),
                    eq(KafkaTopics.WALLET_CREATED_EVENT_TOPIC),
                    eq(expectedJson)
            );
        }
    }

    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {

        @Test
        @DisplayName("Should throw JsonProcessingException when serialization fails")
        public void shouldThrowJsonProcessingException_WhenSerializationFails() throws JsonProcessingException {
            // Arrange
            WalletCreatedEvent event = createValidEvent();
            when(objectMapper.writeValueAsString(event))
                    .thenThrow(new JsonProcessingException("Serialization error") {});

            // Act & Assert
            JsonProcessingException exception = assertThrows(
                    JsonProcessingException.class,
                    () -> eventHandler.handler(event)
            );

            assertNotNull(exception);
            verify(objectMapper, times(1)).writeValueAsString(event);
            verify(outboxService, never()).createOutbox(any(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw DataAccessException when outbox creation fails")
        public void shouldThrowDataAccessException_WhenOutboxCreationFails() throws JsonProcessingException {
            // Arrange
            WalletCreatedEvent event = createValidEvent();
            String eventJson = "{\"userId\":\"test\"}";

            when(objectMapper.writeValueAsString(event)).thenReturn(eventJson);
            doThrow(new DataAccessException("Database error") {})
                    .when(outboxService).createOutbox(any(), anyString(), anyString(), anyString());

            // Act & Assert
            DataAccessException exception = assertThrows(
                    DataAccessException.class,
                    () -> eventHandler.handler(event)
            );

            assertNotNull(exception);
            verify(objectMapper, times(1)).writeValueAsString(event);
            verify(outboxService, times(1)).createOutbox(
                    event.getUserId(),
                    "WalletCreated",
                    KafkaTopics.WALLET_CREATED_EVENT_TOPIC,
                    eventJson
            );
        }

        @Test
        @DisplayName("Should throw PersistenceException when persistence fails")
        public void shouldThrowPersistenceException_WhenPersistenceFails() throws JsonProcessingException {
            // Arrange
            WalletCreatedEvent event = createValidEvent();
            String eventJson = "{\"userId\":\"test\"}";

            when(objectMapper.writeValueAsString(event)).thenReturn(eventJson);
            doThrow(new PersistenceException("Persistence error"))
                    .when(outboxService).createOutbox(any(), anyString(), anyString(), anyString());

            // Act & Assert
            PersistenceException exception = assertThrows(
                    PersistenceException.class,
                    () -> eventHandler.handler(event)
            );

            assertNotNull(exception);
            verify(outboxService, times(1)).createOutbox(
                    event.getUserId(),
                    "WalletCreated",
                    KafkaTopics.WALLET_CREATED_EVENT_TOPIC,
                    eventJson
            );
        }

        @Test
        @DisplayName("Should log error and rethrow exception when outbox save fails")
        public void shouldLogErrorAndRethrowException_WhenOutboxSaveFails() throws JsonProcessingException {
            // Arrange
            WalletCreatedEvent event = createValidEvent();
            String eventJson = "{\"userId\":\"test\"}";

            when(objectMapper.writeValueAsString(event)).thenReturn(eventJson);
            doThrow(new DataAccessException("Connection timeout") {})
                    .when(outboxService).createOutbox(any(), anyString(), anyString(), anyString());

            // Act & Assert
            assertThrows(DataAccessException.class, () -> eventHandler.handler(event));

            verify(objectMapper, times(1)).writeValueAsString(event);
            verify(outboxService, times(1)).createOutbox(
                    event.getUserId(),
                    "WalletCreated",
                    KafkaTopics.WALLET_CREATED_EVENT_TOPIC,
                    eventJson
            );
        }
    }
}

