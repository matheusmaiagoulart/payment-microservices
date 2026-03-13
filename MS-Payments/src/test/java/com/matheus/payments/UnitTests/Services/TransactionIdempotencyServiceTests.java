package com.matheus.payments.UnitTests.Services;

import com.matheus.payments.Application.Audit.TransactionIdempotencyServiceAudit;
import com.matheus.payments.Application.Services.TransactionIdempotencyService;
import com.matheus.payments.Domain.Exceptions.TransactionNotFound;
import com.matheus.payments.Domain.Models.TransactionIdempotency;
import com.matheus.payments.Domain.Repositories.TransactionIdempotencyRepository;
import com.matheus.payments.Infra.Exceptions.Custom.DataBaseException;
import com.matheus.payments.UnitTests.Fixtures.TransactionFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionIdempotencyService Tests")
class TransactionIdempotencyServiceTests {

    @Mock
    private TransactionIdempotencyServiceAudit audit;

    @Mock
    private TransactionIdempotencyRepository transactionRepository;

    @InjectMocks
    private TransactionIdempotencyService transactionIdempotencyService;

    private UUID transactionId;
    private String payload;
    private TransactionIdempotency transactionIdempotency;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        payload = "{\"test\":\"data\"}";
        transactionIdempotency = TransactionFixture.createTransactionIdempotency(transactionId, payload);
    }

    @Nested
    @DisplayName("SUCCESS SCENARIOS")
    class SuccessScenarios {

        @Test
        @DisplayName("Should get transaction by ID successfully")
        public void shouldGetTransactionByIdSuccessfully() {
            // Arrange
            when(transactionRepository.getByTransactionId(any(UUID.class)))
                    .thenReturn(Optional.of(transactionIdempotency));

            // Act
            TransactionIdempotency result = transactionIdempotencyService.getByTransactionId(transactionId);

            // Assert
            assertNotNull(result);
            assertEquals(transactionId, result.getTransactionId());
            assertEquals(payload, result.getPayload());
            verify(transactionRepository, times(1)).getByTransactionId(transactionId);
        }

        @Test
        @DisplayName("Should set transaction as sent successfully")
        public void shouldSetTransactionAsSentSuccessfully() {
            // Arrange
            doNothing().when(transactionRepository).save(any(TransactionIdempotency.class));

            // Act
            transactionIdempotencyService.setTransactionSent(transactionIdempotency);

            // Assert
            assertTrue(transactionIdempotency.getSent());
            verify(transactionRepository, times(1)).save(transactionIdempotency);
        }

        @Test
        @DisplayName("Should save transaction idempotency successfully")
        public void shouldSaveTransactionIdempotencySuccessfully() {
            // Arrange
            doNothing().when(transactionRepository).save(any(TransactionIdempotency.class));

            // Act
            transactionIdempotencyService.save(transactionIdempotency);

            // Assert
            verify(transactionRepository, times(1)).save(transactionIdempotency);
        }

        @Test
        @DisplayName("Should set transaction idempotency as failed successfully")
        public void shouldSetTransactionIdempotencyAsFailed_Successfully() {
            // Arrange
            String errorMessage = "Payment failed";
            doNothing().when(transactionRepository).save(any(TransactionIdempotency.class));

            // Act
            transactionIdempotencyService.setTransactionIdempotencyFailed(transactionIdempotency, errorMessage);

            // Assert
            assertTrue(transactionIdempotency.getFailed());
            assertEquals(errorMessage, transactionIdempotency.getFailureReason());
            verify(transactionRepository, times(1)).save(transactionIdempotency);
        }
    }

    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {

        @Test
        @DisplayName("Should fail when transaction does not exist")
        public void shouldFail_WhenTransactionDoesNotExist() {
            // Arrange
            when(transactionRepository.getByTransactionId(any(UUID.class)))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    TransactionNotFound.class,
                    () -> transactionIdempotencyService.getByTransactionId(transactionId)
            );

            verify(transactionRepository, times(1)).getByTransactionId(transactionId);
        }

        @Test
        @DisplayName("Should fail when save fails")
        public void shouldFail_WhenSaveFails() {
            // Arrange
            doThrow(new DataBaseException("Database connection error"))
                    .when(transactionRepository).save(any(TransactionIdempotency.class));

            // Act & Assert
            DataBaseException exception = assertThrows(
                    DataBaseException.class,
                    () -> transactionIdempotencyService.save(transactionIdempotency)
            );

            assertTrue(exception.getMessage().contains("An error occurred while saving Transaction Idempotency"));
        }

        @Test
        @DisplayName("Should fail when setTransactionSent fails")
        public void shouldFail_WhenSetTransactionSentFails() {
            // Arrange
            doThrow(new DataBaseException("Database error"))
                    .when(transactionRepository).save(any(TransactionIdempotency.class));

            // Act & Assert
            assertThrows(
                    DataBaseException.class,
                    () -> transactionIdempotencyService.setTransactionSent(transactionIdempotency)
            );
        }
    }
}
