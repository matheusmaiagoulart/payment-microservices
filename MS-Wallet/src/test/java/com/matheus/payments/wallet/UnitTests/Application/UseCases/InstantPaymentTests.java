package com.matheus.payments.wallet.UnitTests.Application.UseCases;

import com.matheus.payments.wallet.Application.Audit.WalletServiceAudit;
import com.matheus.payments.wallet.Application.DTOs.Context.PixTransfer;
import com.matheus.payments.wallet.Application.DTOs.Response.InstantPaymentResponse;
import com.matheus.payments.wallet.Application.Services.PixKeyService;
import com.matheus.payments.wallet.Application.Services.TransferExecution;
import com.matheus.payments.wallet.Application.UseCases.InstantPayment;
import com.matheus.payments.wallet.Domain.Exceptions.DomainException;
import com.matheus.payments.wallet.Domain.Exceptions.WalletNotFoundException;
import com.matheus.payments.wallet.Domain.Models.PixKey;
import com.matheus.payments.wallet.Domain.Models.TransactionsProcessed;
import com.matheus.payments.wallet.Infra.Repository.TransactionProcessedRepository;
import com.matheus.payments.wallet.UnitTests.Fixtures.PixKeyFixture;
import com.matheus.payments.wallet.UnitTests.Fixtures.TransactionDTOFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shared.DTOs.TransactionDTO;
import org.shared.Domain.keyType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InstantPaymentTests {

    @Mock
    private WalletServiceAudit audit;

    @Mock
    private PixKeyService pixKeyService;

    @Mock
    private TransferExecution transferExecution;

    @Mock
    private TransactionProcessedRepository transactionsProcessedRepository;

    @InjectMocks
    private InstantPayment instantPayment;

    private void setupValidPixKeys(TransactionDTO request) {
        PixKey senderPixKey = PixKeyFixture.createPixKey(
                request.getSenderKey(), keyType.CPF, request.getSenderAccountId());
        PixKey receiverPixKey = PixKeyFixture.createPixKey(
                request.getReceiverKey(), keyType.CPF, request.getReceiverAccountId());

        when(pixKeyService.getWalletIdByKey(request.getSenderKey())).thenReturn(Optional.of(senderPixKey));
        when(pixKeyService.getWalletIdByKey(request.getReceiverKey())).thenReturn(Optional.of(receiverPixKey));
    }

    @Nested
    @DisplayName("SUCCESS SCENARIOS")
    class SuccessScenarios {

        @Test
        @DisplayName("Should process Instant Payment successfully")
        public void shouldProcessInstantPaymentSuccessfully() {
            // Arrange
            TransactionDTO request = TransactionDTOFixture.createTransactionDTO(new BigDecimal("10.00"));
            setupValidPixKeys(request);

            // Act
            InstantPaymentResponse response = instantPayment.transferProcess(request);

            // Assert - Response
            assertTrue(response.isSucessful());
            assertFalse(response.isAlreadyProcessed());
            assertEquals(response.getReceiverAccountId(), request.getReceiverAccountId());
            assertEquals(response.getSenderAccountId(), request.getSenderAccountId());
            verify(transactionsProcessedRepository, times(1)).saveAndFlush(any());
        }
    }

    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {

        @Test
        @DisplayName("Should fail Instant Payment when sender and receiver are the same")
        public void shouldFailInstantPayment_WhenSenderAndReceiverAreTheSame() {
            // Arrange
            TransactionDTO request = TransactionDTOFixture.createTransactionDTO(new BigDecimal("100"));

            PixKey senderPixKey = PixKeyFixture.createPixKey(request.getSenderKey(), keyType.CPF, request.getSenderAccountId());
            PixKey receiverPixKey = PixKeyFixture.createPixKey(request.getSenderKey(), keyType.CPF, request.getSenderAccountId());

            when(pixKeyService.getWalletIdByKey(request.getSenderKey())).thenReturn(Optional.of(senderPixKey));
            when(pixKeyService.getWalletIdByKey(request.getReceiverKey())).thenReturn(Optional.of(receiverPixKey));

            // Act
            InstantPaymentResponse response = instantPayment.transferProcess(request);

            // Assert - Response
            assertNotNull(response);
            assertFalse(response.isSucessful());
            assertFalse(response.isAlreadyProcessed());
            assertEquals("Sender and Receiver cannot be the same", response.getFailedMessage());

            // Assert - Data consistency
            assertEquals(response.getReceiverAccountId(), response.getSenderAccountId());
            assertEquals(response.getSenderAccountId(), response.getReceiverAccountId());

            // Assert - Verify Operations were called
            verify(transactionsProcessedRepository, times(1)).saveAndFlush(any()); // ← Agora SEMPRE salva primeiro
        }

        @Test
        @DisplayName("Should fail when sender PIX key does not exist")
        public void shouldFail_WhenSenderPixKeyDoesNotExists() {
            // Arrange
            TransactionDTO request = TransactionDTOFixture.createTransactionDTO(new BigDecimal("100"));

            when(pixKeyService.getWalletIdByKey(request.getSenderKey())).thenReturn(Optional.empty());

            // Assert - Response
            WalletNotFoundException exception = assertThrows(WalletNotFoundException.class, () -> {
                instantPayment.transferProcess(request);
            });

            assertNotNull(exception);
            assertEquals(WalletNotFoundException.SENDER_CODE, exception.getErrorCode());
            assertEquals(WalletNotFoundException.class, exception.getClass());

            // Assert - Verify Operations were called
            verify(transactionsProcessedRepository, never()).saveAndFlush(any());
            verify(pixKeyService, times(1)).getWalletIdByKey(request.getSenderKey());
        }

        @Test
        @DisplayName("Should fail when receiver PIX key does not exist")
        public void shouldFail_WhenReceiverPixKeyDoesNotExists() {
            // Arrange
            TransactionDTO request = TransactionDTOFixture.createTransactionDTO(new BigDecimal("100"));

            PixKey senderPixKey = PixKeyFixture.createPixKey(request.getSenderKey(), keyType.CPF, request.getSenderAccountId());

            when(pixKeyService.getWalletIdByKey(request.getSenderKey())).thenReturn(Optional.of(senderPixKey));
            when(pixKeyService.getWalletIdByKey(request.getReceiverKey())).thenReturn(Optional.empty());

            // Assert - Response
            WalletNotFoundException exception = assertThrows(WalletNotFoundException.class, () -> {
                instantPayment.transferProcess(request);
            });

            assertNotNull(exception);
            assertEquals(WalletNotFoundException.RECEIVER_CODE, exception.getErrorCode());
            assertEquals(WalletNotFoundException.class, exception.getClass());

            // Assert - Verify Operations were called
            verify(transactionsProcessedRepository, never()).saveAndFlush(any());
            verify(pixKeyService, times(1)).getWalletIdByKey(request.getSenderKey());
        }

        @Test
        @DisplayName("Should fail when max retry attempts are reached")
        public void shouldFail_WhenMaxRetryAttemptsAreReached() {
            // Arrange
            TransactionDTO request = TransactionDTOFixture.createTransactionDTO(new BigDecimal("10.00"));

            PixKey senderPixKey = PixKeyFixture.createPixKey(request.getSenderKey(), keyType.CPF, request.getSenderAccountId());
            PixKey receiverPixKey = PixKeyFixture.createPixKey(request.getReceiverKey(), keyType.CPF, request.getReceiverAccountId());

            when(pixKeyService.getWalletIdByKey(request.getSenderKey())).thenReturn(Optional.of(senderPixKey));
            when(pixKeyService.getWalletIdByKey(request.getReceiverKey())).thenReturn(Optional.of(receiverPixKey));

            doThrow(new OptimisticLockingFailureException("Concurrent update detected"))
                    .when(transferExecution).transferExecutionWithRetry(any(PixTransfer.class));

            // Act
            InstantPaymentResponse response = instantPayment.transferProcess(request);

            // Assert - Response
            assertNotNull(response);
            assertFalse(response.isSucessful());
            assertFalse(response.isAlreadyProcessed());
            assertEquals("Concurrent transaction conflict", response.getFailedMessage());

            // Assert - Data consistency
            assertEquals(response.getReceiverAccountId(), request.getReceiverAccountId());
            assertEquals(response.getSenderAccountId(), request.getSenderAccountId());

            // Assert - Verify Operations were called
            verify(transferExecution, times(1)).transferExecutionWithRetry(any());
            verify(transactionsProcessedRepository, times(1)).saveAndFlush(any()); // ← Salva ANTES da transferência
        }

        @Test
        @DisplayName("Should return idempotency response when DataIntegrityViolationException is thrown on save")
        public void shouldReturnIdempotencyResponse_WhenDataIntegrityViolationExceptionIsThrown() {
            // Arrange
            TransactionDTO request = TransactionDTOFixture.createTransactionDTO(new BigDecimal("10.00"));

            PixKey senderPixKey = PixKeyFixture.createPixKey(request.getSenderKey(), keyType.CPF, request.getSenderAccountId());
            PixKey receiverPixKey = PixKeyFixture.createPixKey(request.getReceiverKey(), keyType.CPF, request.getReceiverAccountId());

            when(pixKeyService.getWalletIdByKey(request.getSenderKey())).thenReturn(Optional.of(senderPixKey));
            when(pixKeyService.getWalletIdByKey(request.getReceiverKey())).thenReturn(Optional.of(receiverPixKey));

            doThrow(DataIntegrityViolationException.class)
                    .when(transactionsProcessedRepository).saveAndFlush(any(TransactionsProcessed.class));

            // Act
            InstantPaymentResponse response = instantPayment.transferProcess(request);

            // Assert - Response
            assertNotNull(response);
            assertTrue(response.isSucessful());
            assertTrue(response.isAlreadyProcessed());
            assertEquals("Transaction has already been processed.", response.getFailedMessage());

            // Assert - Data consistency
            assertEquals(response.getReceiverAccountId(), request.getReceiverAccountId());
            assertEquals(response.getSenderAccountId(), request.getSenderAccountId());

            // Assert - Verify Operations were called
            verify(transactionsProcessedRepository, times(1)).saveAndFlush(any());
            verify(transferExecution, never()).transferExecutionWithRetry(any()); // ← NÃO executa transferência!
        }

        @Test
        @DisplayName("Should return failed transfer when TransferExecution throws DomainException")
        void shouldReturnFailedTransfer_WhenTransferExecutionThrowsDomainException() {
            TransactionDTO request = TransactionDTOFixture.createTransactionDTO(new BigDecimal("10.00"));

            PixKey senderPixKey = PixKeyFixture.createPixKey(request.getSenderKey(), keyType.CPF, request.getSenderAccountId());
            PixKey receiverPixKey = PixKeyFixture.createPixKey(request.getReceiverKey(), keyType.CPF, request.getReceiverAccountId());

            when(pixKeyService.getWalletIdByKey(request.getSenderKey())).thenReturn(Optional.of(senderPixKey));
            when(pixKeyService.getWalletIdByKey(request.getReceiverKey())).thenReturn(Optional.of(receiverPixKey));

            DomainException domainException = new DomainException("DOMAIN ERROR", "domain error") {
            };

            doThrow(domainException)
                    .when(transferExecution).transferExecutionWithRetry(any(PixTransfer.class));

            InstantPaymentResponse response = instantPayment.transferProcess(request);

            assertNotNull(response);
            assertFalse(response.isSucessful());
            assertFalse(response.isAlreadyProcessed());
            assertEquals("domain error", response.getFailedMessage());
            verify(transactionsProcessedRepository, times(1)).saveAndFlush(any()); // ← Salva ANTES da transferência
        }
    }
}
