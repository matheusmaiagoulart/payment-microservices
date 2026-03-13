package com.matheus.payments.UnitTests.Services;

import com.matheus.payments.Application.Audit.TransactionServiceAudit;
import com.matheus.payments.Application.DTOs.TransactionRequest;
import com.matheus.payments.Application.Mappers.TransactionMapper;
import com.matheus.payments.Application.Services.TransactionService;
import com.matheus.payments.Domain.Exceptions.TransactionNotFound;
import com.matheus.payments.Domain.Models.Transaction;
import com.matheus.payments.Domain.Repositories.TransactionRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Tests")
class TransactionServiceTests {

    @Mock
    private TransactionServiceAudit audit;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private TransactionRequest transactionRequest;
    private Transaction transaction;
    private UUID transactionId;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        transactionRequest = TransactionFixture.createTransactionRequest();
        transaction = TransactionFixture.createTransaction();
    }

    @Nested
    @DisplayName("SUCCESS SCENARIOS")
    class SuccessScenarios {

        @Test
        @DisplayName("Should create payment process successfully")
        public void shouldCreatePaymentProcessSuccessfully() {
            // Arrange
            when(transactionMapper.mapToEntity(any(TransactionRequest.class))).thenReturn(transaction);
            doNothing().when(transactionRepository).save(any(Transaction.class));

            // Act
            String result = transactionService.createPaymentProcess(transactionRequest);

            // Assert
            assertNotNull(result);
            verify(transactionMapper, times(1)).mapToEntity(transactionRequest);
            verify(audit, times(1)).logCreateTransaction(anyString());
            verify(transactionRepository, times(1)).save(transaction);
        }

        @Test
        @DisplayName("Should get transaction by ID successfully")
        public void shouldGetTransactionByIdSuccessfully() {
            // Arrange
            when(transactionRepository.findByTransactionId(any(UUID.class)))
                    .thenReturn(Optional.of(transaction));

            // Act
            Transaction result = transactionService.getTransactionById(transactionId);

            // Assert
            assertNotNull(result);
            assertEquals(TransactionFixture.SENDER_KEY, result.getSenderKey());
            assertEquals(TransactionFixture.RECEIVER_KEY, result.getReceiverKey());
            verify(transactionRepository, times(1)).findByTransactionId(transactionId);
        }

        @Test
        @DisplayName("Should save transaction successfully")
        public void shouldSaveTransactionSuccessfully() {
            // Arrange
            doNothing().when(transactionRepository).save(any(Transaction.class));

            // Act
            transactionService.save(transaction);

            // Assert
            verify(transactionRepository, times(1)).save(transaction);
        }

        @Test
        @DisplayName("Should return valid UUID as string")
        public void shouldReturnValidUUID_AsString() {
            // Arrange
            when(transactionMapper.mapToEntity(any(TransactionRequest.class))).thenReturn(transaction);
            doNothing().when(transactionRepository).save(any(Transaction.class));

            // Act
            String result = transactionService.createPaymentProcess(transactionRequest);

            // Assert
            assertDoesNotThrow(() -> UUID.fromString(result));
        }
    }

    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {

        @Test
        @DisplayName("Should fail when transaction does not exist")
        public void shouldFail_WhenTransactionDoesNotExist() {
            // Arrange
            when(transactionRepository.findByTransactionId(any(UUID.class)))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    TransactionNotFound.class,
                    () -> transactionService.getTransactionById(transactionId)
            );

            verify(transactionRepository, times(1)).findByTransactionId(transactionId);
        }

        @Test
        @DisplayName("Should fail when create payment process fails")
        public void shouldFail_WhenCreatePaymentProcessFails() {
            // Arrange
            when(transactionMapper.mapToEntity(any(TransactionRequest.class))).thenReturn(transaction);
            doThrow(new RuntimeException("Database error"))
                    .when(transactionRepository).save(any(Transaction.class));

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> transactionService.createPaymentProcess(transactionRequest)
            );

            assertEquals("Database error", exception.getMessage());
        }

        @Test
        @DisplayName("Should fail when save transaction fails")
        public void shouldFail_WhenSaveTransactionFails() {
            // Arrange
            doThrow(new RuntimeException("Database error"))
                    .when(transactionRepository).save(any(Transaction.class));

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> transactionService.save(transaction)
            );

            assertEquals("Database error", exception.getMessage());
        }

        @Test
        @DisplayName("Should fail when mapper fails")
        public void shouldFail_WhenMapperFails() {
            // Arrange
            when(transactionMapper.mapToEntity(any(TransactionRequest.class)))
                    .thenThrow(new RuntimeException("Mapping error"));

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> transactionService.createPaymentProcess(transactionRequest)
            );

            assertEquals("Mapping error", exception.getMessage());
            verify(transactionRepository, never()).save(any());
        }
    }
}
