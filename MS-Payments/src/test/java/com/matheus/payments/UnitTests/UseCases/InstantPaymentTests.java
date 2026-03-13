package com.matheus.payments.UnitTests.UseCases;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.Application.Audit.InstantPaymentFacadeAudit;
import com.matheus.payments.Application.DTOs.TransactionRequest;
import com.matheus.payments.Application.Services.PaymentProcessorService;
import com.matheus.payments.Application.Services.TransactionIdempotencyService;
import com.matheus.payments.Application.Services.TransactionService;
import com.matheus.payments.Application.UseCases.InstantPayment;
import com.matheus.payments.UnitTests.Fixtures.TransactionFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shared.DTOs.PaymentProcessorResponse;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InstantPayment Use Case Tests")
class InstantPaymentTests {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TransactionIdempotencyService idempotencyService;

    @Mock
    private InstantPaymentFacadeAudit audit;

    @Mock
    private TransactionService transactionService;

    @Mock
    private PaymentProcessorService paymentProcessorService;

    @InjectMocks
    private InstantPayment instantPayment;

    private TransactionRequest transactionRequest;
    private String transactionId;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID().toString();
        transactionRequest = TransactionFixture.createTransactionRequest();
    }

    @Nested
    @DisplayName("SUCCESS SCENARIOS")
    class SuccessScenarios {

        @Test
        @DisplayName("Should orchestrate payment successfully")
        public void shouldOrchestratePaymentSuccessfully() throws IOException {
            // Arrange
            PaymentProcessorResponse successResponse = mock(PaymentProcessorResponse.class);
            when(successResponse.getIsSuccessful()).thenReturn(true);
            
            when(transactionService.createPaymentProcess(any(TransactionRequest.class))).thenReturn(transactionId);
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");
            when(paymentProcessorService.sendPaymentToProcessor(anyString())).thenReturn(successResponse);
            when(paymentProcessorService.paymentStatusUpdate(any(PaymentProcessorResponse.class)))
                    .thenReturn(successResponse);

            // Act
            PaymentProcessorResponse result = instantPayment.paymentOrchestration(transactionRequest);

            // Assert
            assertNotNull(result);
            assertTrue(result.getIsSuccessful());
            verify(transactionService, times(1)).createPaymentProcess(any());
            verify(idempotencyService, times(1)).createTransactionIdempotencyEntry(eq(transactionId), anyString());
            verify(audit, times(1)).logPaymentProcessStarting(transactionId);
            verify(paymentProcessorService, times(1)).sendPaymentToProcessor(transactionId);
        }

        @Test
        @DisplayName("Should create idempotency entry before sending payment")
        public void shouldCreateIdempotencyEntry_BeforeSendingPayment() throws IOException {
            // Arrange
            PaymentProcessorResponse successResponse = mock(PaymentProcessorResponse.class);
            
            when(transactionService.createPaymentProcess(any(TransactionRequest.class))).thenReturn(transactionId);
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");
            when(paymentProcessorService.sendPaymentToProcessor(anyString())).thenReturn(successResponse);
            when(paymentProcessorService.paymentStatusUpdate(any(PaymentProcessorResponse.class)))
                    .thenReturn(successResponse);

            // Act
            instantPayment.paymentOrchestration(transactionRequest);

            // Assert
            verify(idempotencyService, times(1)).createTransactionIdempotencyEntry(anyString(), anyString());
            verify(paymentProcessorService, times(1)).sendPaymentToProcessor(transactionId);
        }
    }

    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {

        @Test
        @DisplayName("Should fail when transaction creation fails")
        public void shouldFail_WhenTransactionCreationFails() {
            // Arrange
            when(transactionService.createPaymentProcess(any(TransactionRequest.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> instantPayment.paymentOrchestration(transactionRequest)
            );

            assertEquals("Database error", exception.getMessage());
            verify(paymentProcessorService, never()).sendPaymentToProcessor(anyString());
        }

        @Test
        @DisplayName("Should fail when idempotency creation fails")
        public void shouldFail_WhenIdempotencyCreationFails() throws IOException {
            // Arrange
            when(transactionService.createPaymentProcess(any(TransactionRequest.class))).thenReturn(transactionId);
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");
            doThrow(new RuntimeException("Idempotency error"))
                    .when(idempotencyService).createTransactionIdempotencyEntry(anyString(), anyString());

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> instantPayment.paymentOrchestration(transactionRequest)
            );

            assertEquals("Idempotency error", exception.getMessage());
            verify(paymentProcessorService, never()).sendPaymentToProcessor(anyString());
        }

        @Test
        @DisplayName("Should fail when payment processor fails")
        public void shouldFail_WhenPaymentProcessorFails() throws IOException {
            // Arrange
            when(transactionService.createPaymentProcess(any(TransactionRequest.class))).thenReturn(transactionId);
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");
            when(paymentProcessorService.sendPaymentToProcessor(anyString()))
                    .thenThrow(new RuntimeException("Payment processor error"));

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> instantPayment.paymentOrchestration(transactionRequest)
            );

            assertEquals("Payment processor error", exception.getMessage());
            verify(paymentProcessorService, never()).paymentStatusUpdate(any());
        }
    }
}
