package com.matheus.payments.wallet.UnitTests.Application.EventHandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.wallet.Application.EventHandlers.WalletHandlers.WalletCreationFailedEventHandler;
import com.matheus.payments.wallet.Domain.Events.CreateWallet.WalletCreationFailed;
import com.matheus.payments.wallet.Application.Services.OutboxService;
import com.matheus.payments.wallet.UnitTests.Fixtures.WalletCreationFailedFixture;
import com.matheus.payments.wallet.utils.KafkaTopics;
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
public class WalletCreationFailedEventHandlerTests {

    @Mock
    private OutboxService outboxService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WalletCreationFailedEventHandler eventHandler;

    private WalletCreationFailed createValidEvent() {
        return WalletCreationFailedFixture.createWalletCreationFailed(
                UUID.randomUUID(),
                "11111111111",
                "Error creating wallet"
        );
    }

    @Nested
    @DisplayName("SUCCESS SCENARIOS")
    class SuccessScenarios {

        @Test
        @DisplayName("Should handle wallet creation failed event successfully")
        public void shouldHandleWalletCreationFailedEventSuccessfully() throws JsonProcessingException {
            // Arrange
            WalletCreationFailed event = createValidEvent();
            String eventJson = "{\"userId\":\"" + event.getUserId() + "\",\"cpf\":\"" + event.getCpf() + "\",\"errorMessage\":\"" + event.getErrorMessage() + "\"}";

            when(objectMapper.writeValueAsString(event)).thenReturn(eventJson);
            doNothing().when(outboxService).createOutbox(
                    eq(event.getUserId()),
                    eq("WalletCreationFailed"),
                    eq(KafkaTopics.WALLET_CREATION_FAILED_TOPIC),
                    eq(eventJson)
            );

            // Act
            assertDoesNotThrow(() -> eventHandler.handler(event));

            // Assert
            verify(objectMapper, times(1)).writeValueAsString(event);
            verify(outboxService, times(1)).createOutbox(
                    event.getUserId(),
                    "WalletCreationFailed",
                    KafkaTopics.WALLET_CREATION_FAILED_TOPIC,
                    eventJson
            );
        }

        @Test
        @DisplayName("Should serialize event with error message correctly")
        public void shouldSerializeEventWithErrorMessageCorrectly() throws JsonProcessingException {
            // Arrange
            WalletCreationFailed event = createValidEvent();
            String expectedJson = "{\"userId\":\"test\",\"cpf\":\"11111111111\",\"errorMessage\":\"Error\"}";

            when(objectMapper.writeValueAsString(event)).thenReturn(expectedJson);
            doNothing().when(outboxService).createOutbox(any(), anyString(), anyString(), anyString());

            // Act
            assertDoesNotThrow(() -> eventHandler.handler(event));

            // Assert
            verify(objectMapper, times(1)).writeValueAsString(event);
            verify(outboxService, times(1)).createOutbox(
                    any(UUID.class),
                    eq("WalletCreationFailed"),
                    eq(KafkaTopics.WALLET_CREATION_FAILED_TOPIC),
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
            WalletCreationFailed event = createValidEvent();
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
        @DisplayName("Should throw exception when outbox creation fails")
        public void shouldThrowException_WhenOutboxCreationFails() throws JsonProcessingException {
            // Arrange
            WalletCreationFailed event = createValidEvent();
            String eventJson = "{\"userId\":\"test\"}";

            when(objectMapper.writeValueAsString(event)).thenReturn(eventJson);
            doThrow(new DataAccessException("Database error") {})
                    .when(outboxService).createOutbox(any(), anyString(), anyString(), anyString());

            // Act & Assert
            Exception exception = assertThrows(
                    Exception.class,
                    () -> eventHandler.handler(event)
            );

            assertNotNull(exception);
            verify(objectMapper, times(1)).writeValueAsString(event);
            verify(outboxService, times(1)).createOutbox(
                    event.getUserId(),
                    "WalletCreationFailed",
                    KafkaTopics.WALLET_CREATION_FAILED_TOPIC,
                    eventJson
            );
        }

        @Test
        @DisplayName("Should log error and rethrow when unexpected exception occurs")
        public void shouldLogErrorAndRethrow_WhenUnexpectedExceptionOccurs() throws JsonProcessingException {
            // Arrange
            WalletCreationFailed event = createValidEvent();
            String eventJson = "{\"userId\":\"test\"}";

            when(objectMapper.writeValueAsString(event)).thenReturn(eventJson);
            doThrow(new RuntimeException("Unexpected error"))
                    .when(outboxService).createOutbox(any(), anyString(), anyString(), anyString());

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> eventHandler.handler(event)
            );

            assertNotNull(exception);
            assertEquals("Unexpected error", exception.getMessage());
            verify(outboxService, times(1)).createOutbox(
                    event.getUserId(),
                    "WalletCreationFailed",
                    KafkaTopics.WALLET_CREATION_FAILED_TOPIC,
                    eventJson
            );
        }

        @Test
        @DisplayName("Should handle multiple error scenarios")
        public void shouldHandleMultipleErrorScenarios() throws JsonProcessingException {
            // Arrange
            WalletCreationFailed event = createValidEvent();
            String eventJson = "{\"userId\":\"test\"}";

            when(objectMapper.writeValueAsString(event)).thenReturn(eventJson);
            doThrow(new IllegalArgumentException("Invalid argument"))
                    .when(outboxService).createOutbox(any(), anyString(), anyString(), anyString());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> eventHandler.handler(event));

            verify(objectMapper, times(1)).writeValueAsString(event);
            verify(outboxService, times(1)).createOutbox(
                    event.getUserId(),
                    "WalletCreationFailed",
                    KafkaTopics.WALLET_CREATION_FAILED_TOPIC,
                    eventJson
            );
        }
    }
}

