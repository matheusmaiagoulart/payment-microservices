package com.matheus.payments.UnitTests.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.Application.Audit.PaymentProcessorAudit;
import com.matheus.payments.Application.Services.PaymentProcessorService;
import com.matheus.payments.Application.Services.TransactionIdempotencyService;
import com.matheus.payments.Application.Services.TransactionService;
import com.matheus.payments.Domain.Exceptions.TransactionFailedException;
import com.matheus.payments.Domain.Models.Transaction;
import com.matheus.payments.Domain.Models.TransactionIdempotency;
import com.matheus.payments.Infra.Exceptions.Custom.TransactionAlreadySentException;
import com.matheus.payments.Infra.Http.WalletService;
import com.matheus.payments.UnitTests.Fixtures.TransactionFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.shared.DTOs.PaymentProcessorResponse;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PaymentProcessorService Tests")
class PaymentProcessorServiceTests {

    @Mock
    private PaymentProcessorAudit audit;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private TransactionIdempotencyService idempotencyService;

    @Mock
    private WalletService walletServerRequest;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private PaymentProcessorService paymentProcessorService;

    private String transactionId;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID().toString();
        transaction = TransactionFixture.createTransaction();
    }

    @Nested
    @DisplayName("SUCCESS SCENARIOS")
    class SuccessScenarios {

        @Test
        @DisplayName("Should send payment to processor successfully")
        public void shouldSendPaymentToProcessorSuccessfully() throws IOException, TimeoutException, InterruptedException {
            // Arrange
            TransactionIdempotency idempotency = TransactionFixture.createTransactionIdempotency(
                    UUID.fromString(transactionId), "{\"test\":\"data\"}");
            
            PaymentProcessorResponse successResponse = mock(PaymentProcessorResponse.class);
            when(successResponse.getTransactionId()).thenReturn(UUID.fromString(transactionId));
            when(successResponse.getIsSuccessful()).thenReturn(true);
            when(successResponse.getIsSent()).thenReturn(true);
            when(successResponse.isAlreadyProcessed()).thenReturn(false);
            when(successResponse.getSenderAccountId()).thenReturn(UUID.randomUUID());
            when(successResponse.getReceiverAccountId()).thenReturn(UUID.randomUUID());
            
            HttpResponse<String> httpResponse = mock(HttpResponse.class);
            when(httpResponse.body()).thenReturn("{\"isSuccessful\":true}");

            when(idempotencyService.getByTransactionId(any(UUID.class))).thenReturn(idempotency);
            when(walletServerRequest.instantPaymentRequest(anyString())).thenReturn(httpResponse);
            when(mapper.readValue(anyString(), eq(PaymentProcessorResponse.class))).thenReturn(successResponse);
            when(transactionService.getTransactionById(any(UUID.class))).thenReturn(transaction);

            // Act
            PaymentProcessorResponse result = paymentProcessorService.sendPaymentToProcessor(transactionId);

            // Assert
            assertNotNull(result);
            assertTrue(result.getIsSuccessful());
            verify(audit, times(1)).logSendingRequestWallet(transactionId);
            verify(audit, times(1)).logSentSuccessfullyWallet(transactionId);
        }

        @Test
        @DisplayName("Should update payment status to COMPLETED when successful")
        public void shouldUpdatePaymentStatus_ToCompleted_WhenSuccessful() {
            // Arrange
            TransactionIdempotency idempotency = TransactionFixture.createTransactionIdempotency(
                    UUID.fromString(transactionId), "{\"test\":\"data\"}");
            
            PaymentProcessorResponse successResponse = mock(PaymentProcessorResponse.class);
            when(successResponse.getTransactionId()).thenReturn(UUID.fromString(transactionId));
            when(successResponse.getIsSuccessful()).thenReturn(true);
            when(successResponse.getIsSent()).thenReturn(true);
            when(successResponse.isAlreadyProcessed()).thenReturn(false);
            when(successResponse.getSenderAccountId()).thenReturn(UUID.randomUUID());
            when(successResponse.getReceiverAccountId()).thenReturn(UUID.randomUUID());
            
            when(transactionService.getTransactionById(any(UUID.class))).thenReturn(transaction);
            when(idempotencyService.getByTransactionId(any(UUID.class))).thenReturn(idempotency);

            // Act
            PaymentProcessorResponse result = paymentProcessorService.paymentStatusUpdate(successResponse);

            // Assert
            assertNotNull(result);
            verify(audit, times(1)).logReceivedSuccessResponse(anyString());
            verify(transactionService, times(1)).save(transaction);
        }

        @Test
        @DisplayName("Should handle already processed transaction")
        public void shouldHandleAlreadyProcessedTransaction() {
            // Arrange
            TransactionIdempotency idempotency = TransactionFixture.createTransactionIdempotency(
                    UUID.fromString(transactionId), "{\"test\":\"data\"}");
            
            PaymentProcessorResponse alreadyProcessedResponse = mock(PaymentProcessorResponse.class);
            when(alreadyProcessedResponse.getTransactionId()).thenReturn(UUID.fromString(transactionId));
            when(alreadyProcessedResponse.getIsSent()).thenReturn(true);
            when(alreadyProcessedResponse.isAlreadyProcessed()).thenReturn(true);
            
            when(transactionService.getTransactionById(any(UUID.class))).thenReturn(transaction);
            when(idempotencyService.getByTransactionId(any(UUID.class))).thenReturn(idempotency);

            // Act
            PaymentProcessorResponse result = paymentProcessorService.paymentStatusUpdate(alreadyProcessedResponse);

            // Assert
            assertNotNull(result);
            verify(audit, times(1)).logReceivedSuccessResponse(anyString());
        }
    }

    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {

        @Test
        @DisplayName("Should fail when transaction already sent")
        public void shouldFail_WhenTransactionAlreadySent() {
            // Arrange
            TransactionIdempotency idempotency = mock(TransactionIdempotency.class);
            when(idempotency.getSent()).thenReturn(true);
            when(idempotency.getTransactionId()).thenReturn(UUID.fromString(transactionId));
            when(idempotencyService.getByTransactionId(any(UUID.class))).thenReturn(idempotency);

            // Act & Assert
            TransactionAlreadySentException exception = assertThrows(
                    TransactionAlreadySentException.class,
                    () -> paymentProcessorService.sendPaymentToProcessor(transactionId)
            );

            assertTrue(exception.getMessage().contains("has already been sent"));
        }

        @Test
        @DisplayName("Should fail when wallet service fails")
        public void shouldFail_WhenWalletServiceFails() throws IOException, TimeoutException, InterruptedException {
            // Arrange
            TransactionIdempotency idempotency = TransactionFixture.createTransactionIdempotency(
                    UUID.fromString(transactionId), "{\"test\":\"data\"}");
            
            when(idempotencyService.getByTransactionId(any(UUID.class))).thenReturn(idempotency);
            when(walletServerRequest.instantPaymentRequest(anyString()))
                    .thenThrow(new IOException("Connection error"));
            when(transactionService.getTransactionById(any(UUID.class))).thenReturn(transaction);

            // Act & Assert
            TransactionFailedException exception = assertThrows(
                    TransactionFailedException.class,
                    () -> paymentProcessorService.sendPaymentToProcessor(transactionId)
            );

            assertTrue(exception.getMessage().contains("Error sending payment to processor"));
        }

        @Test
        @DisplayName("Should fail when payment fails")
        public void shouldFail_WhenPaymentFails() {
            // Arrange
            TransactionIdempotency idempotency = TransactionFixture.createTransactionIdempotency(
                    UUID.fromString(transactionId), "{\"test\":\"data\"}");
            
            PaymentProcessorResponse failedResponse = mock(PaymentProcessorResponse.class);
            when(failedResponse.getTransactionId()).thenReturn(UUID.fromString(transactionId));
            when(failedResponse.getIsSent()).thenReturn(true);
            when(failedResponse.getIsSuccessful()).thenReturn(false);
            when(failedResponse.getFailedMessage()).thenReturn("Insufficient funds");
            when(failedResponse.isAlreadyProcessed()).thenReturn(false);
            
            when(transactionService.getTransactionById(any(UUID.class))).thenReturn(transaction);
            when(idempotencyService.getByTransactionId(any(UUID.class))).thenReturn(idempotency);

            // Act & Assert
            TransactionFailedException exception = assertThrows(
                    TransactionFailedException.class,
                    () -> paymentProcessorService.paymentStatusUpdate(failedResponse)
            );

            assertTrue(exception.getMessage().contains("Insufficient funds"));
        }

        @Test
        @DisplayName("Should fail when payment is not sent")
        public void shouldFail_WhenPaymentIsNotSent() {
            // Arrange
            TransactionIdempotency idempotency = TransactionFixture.createTransactionIdempotency(
                    UUID.fromString(transactionId), "{\"test\":\"data\"}");
            
            PaymentProcessorResponse notSentResponse = mock(PaymentProcessorResponse.class);
            when(notSentResponse.getTransactionId()).thenReturn(UUID.fromString(transactionId));
            when(notSentResponse.getIsSent()).thenReturn(false);
            when(notSentResponse.getFailedMessage()).thenReturn("Connection failed");
            
            when(transactionService.getTransactionById(any(UUID.class))).thenReturn(transaction);
            when(idempotencyService.getByTransactionId(any(UUID.class))).thenReturn(idempotency);

            // Act & Assert
            TransactionFailedException exception = assertThrows(
                    TransactionFailedException.class,
                    () -> paymentProcessorService.paymentStatusUpdate(notSentResponse)
            );

            assertTrue(exception.getMessage().contains("Connection failed"));
        }

        @Test
        @DisplayName("Should fail when timeout occurs")
        public void shouldFail_WhenTimeoutOccurs() throws IOException, TimeoutException, InterruptedException {
            // Arrange
            TransactionIdempotency idempotency = TransactionFixture.createTransactionIdempotency(
                    UUID.fromString(transactionId), "{\"test\":\"data\"}");
            
            when(idempotencyService.getByTransactionId(any(UUID.class))).thenReturn(idempotency);
            when(walletServerRequest.instantPaymentRequest(anyString()))
                    .thenThrow(new TimeoutException("Request timeout"));
            when(transactionService.getTransactionById(any(UUID.class))).thenReturn(transaction);

            // Act & Assert
            TransactionFailedException exception = assertThrows(
                    TransactionFailedException.class,
                    () -> paymentProcessorService.sendPaymentToProcessor(transactionId)
            );

            assertTrue(exception.getMessage().contains("Error sending payment to processor"));
        }
    }
}

