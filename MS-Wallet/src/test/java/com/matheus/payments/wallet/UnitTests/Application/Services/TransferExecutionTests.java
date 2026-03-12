package com.matheus.payments.wallet.UnitTests.Application.Services;

import com.matheus.payments.wallet.Application.Audit.WalletServiceAudit;
import com.matheus.payments.wallet.Application.DTOs.Context.PixTransfer;
import com.matheus.payments.wallet.Application.DTOs.Context.TransferResult;
import com.matheus.payments.wallet.Application.Services.LedgerService;
import com.matheus.payments.wallet.Application.Services.TransferExecution;
import com.matheus.payments.wallet.Application.Services.WalletService;
import com.matheus.payments.wallet.Domain.Exceptions.InsufficientBalanceException;
import com.matheus.payments.wallet.Domain.Exceptions.InvalidAmountException;
import com.matheus.payments.wallet.Domain.Exceptions.WalletNotFoundException;
import com.matheus.payments.wallet.Domain.Models.PixKey;
import com.matheus.payments.wallet.Domain.Models.Wallet;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.FailedToSaveLedgerEntry;
import com.matheus.payments.wallet.UnitTests.Fixtures.PixKeyFixture;
import com.matheus.payments.wallet.UnitTests.Fixtures.PixTransferFixture;
import com.matheus.payments.wallet.UnitTests.Fixtures.TransactionDTOFixture;
import com.matheus.payments.wallet.UnitTests.Fixtures.WalletFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shared.DTOs.TransactionDTO;
import org.shared.Domain.accountType;
import org.shared.Domain.keyType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransferExecutionTests {

    @Mock
    private WalletServiceAudit audit;
    @Mock
    private LedgerService ledgerService;
    @Mock
    private WalletService walletService;

    @InjectMocks
    private TransferExecution transferExecution;

    @Nested
    @DisplayName("SUCCESS SCENARIOS")
    class SuccessScenarios {
        @Test
        @DisplayName("Should process Transfer successfully when balances are exact")
        public void shouldProcessTransferSuccessfully_WhenBalanceAreExact() throws Exception {

            ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
            BigDecimal amount = new BigDecimal("10.00");
            BigDecimal senderInitialBalance = new BigDecimal("10.00");

            TransactionDTO request = TransactionDTOFixture.createTransactionDTO(amount);

            Wallet senderWallet = WalletFixture.createWallet(
                    request.getSenderAccountId(),
                    accountType.CHECKING,
                    request.getSenderKey());
            senderWallet.setBalance(senderInitialBalance);

            Wallet receiverWallet = WalletFixture.createWallet(
                    request.getReceiverAccountId(),
                    accountType.CHECKING,
                    request.getReceiverKey());

            PixKey receiverKey = PixKeyFixture.createPixKey(
                    request.getReceiverKey(),
                    keyType.CPF,
                    request.getReceiverAccountId());

            PixKey senderKey = PixKeyFixture.createPixKey(
                    request.getSenderKey(),
                    keyType.CPF,
                    request.getSenderAccountId());

            PixTransfer pixTransfer = PixTransferFixture.createPixTransfer(request, senderKey, receiverKey);

            when(walletService.getWalletById(request.getSenderAccountId()))
                    .thenReturn(Optional.of(senderWallet));
            when(walletService.getWalletById(request.getReceiverAccountId()))
                    .thenReturn(Optional.of(receiverWallet));

            // Act - Should not throw exception
            assertDoesNotThrow(() -> transferExecution.transferExecutionWithRetry(pixTransfer));

            // Assert - Data consistency
            verify(walletService, times(2)).saveWallet(walletCaptor.capture());
            Wallet senderFinalWallet = walletCaptor
                    .getAllValues()
                    .stream()
                    .filter(x -> x.getAccountId().equals(request.getSenderAccountId()))
                    .findFirst()
                    .orElseThrow();

            Wallet receiverFinalWallet = walletCaptor
                    .getAllValues()
                    .stream()
                    .filter(x -> x.getAccountId().equals(request.getReceiverAccountId()))
                    .findFirst()
                    .orElseThrow();

            assertEquals(new BigDecimal("0.00"), senderFinalWallet.getBalance());
            assertEquals(amount, receiverFinalWallet.getBalance());

            verify(walletService, times(2)).getWalletById(any());
            verify(ledgerService, times(1)).registryLedgeEntries(any(PixTransfer.class));
        }

        @Test
        @DisplayName("Should process Transfer successfully when balances has multiple decimal places")
        public void shouldProcessTransferSuccessfully_WhenBalanceHasMultipleDecimalPlaces() throws Exception {

            ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
            BigDecimal amount = new BigDecimal("10.5978"); // round to 10.60
            BigDecimal senderInitialBalance = new BigDecimal("20.00");

            TransactionDTO request = TransactionDTOFixture.createTransactionDTO(amount);

            Wallet senderWallet = WalletFixture.createWallet(
                    request.getSenderAccountId(),
                    accountType.CHECKING,
                    request.getSenderKey());
            senderWallet.setBalance(senderInitialBalance);

            Wallet receiverWallet = WalletFixture.createWallet(
                    request.getReceiverAccountId(),
                    accountType.CHECKING,
                    request.getReceiverKey());

            PixKey receiverKey = PixKeyFixture.createPixKey(
                    request.getReceiverKey(),
                    keyType.CPF,
                    request.getReceiverAccountId());

            PixKey senderKey = PixKeyFixture.createPixKey(
                    request.getSenderKey(),
                    keyType.CPF,
                    request.getSenderAccountId());

            PixTransfer pixTransfer = PixTransferFixture.createPixTransfer(request, senderKey, receiverKey);

            when(walletService.getWalletById(request.getSenderAccountId()))
                    .thenReturn(Optional.of(senderWallet));
            when(walletService.getWalletById(request.getReceiverAccountId()))
                    .thenReturn(Optional.of(receiverWallet));

            // Act - Should not throw exception
            assertDoesNotThrow(() -> transferExecution.transferExecutionWithRetry(pixTransfer));

            // Assert - Data consistency
            verify(walletService, times(2)).saveWallet(walletCaptor.capture());
            Wallet senderFinalWallet = walletCaptor
                    .getAllValues()
                    .stream()
                    .filter(x -> x.getAccountId().equals(request.getSenderAccountId()))
                    .findFirst()
                    .orElseThrow();

            Wallet receiverFinalWallet = walletCaptor
                    .getAllValues()
                    .stream()
                    .filter(x -> x.getAccountId().equals(request.getReceiverAccountId()))
                    .findFirst()
                    .orElseThrow();

            assertEquals(new BigDecimal("9.40"), senderFinalWallet.getBalance());
            assertEquals(amount.setScale(2, RoundingMode.HALF_UP), receiverFinalWallet.getBalance());

            verify(walletService, times(2)).getWalletById(any());
            verify(ledgerService, times(1)).registryLedgeEntries(any(PixTransfer.class));
        }
    }

    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {
        @Test
        @DisplayName("Should fail when sender has insufficient balance")
        public void shouldFail_WhenSenderHasInsufficientBalance() {

            BigDecimal amount = new BigDecimal("100");
            BigDecimal senderInitialBalance = new BigDecimal("10");

            TransactionDTO request = TransactionDTOFixture.createTransactionDTO(amount);

            Wallet senderWallet = WalletFixture.createWallet(
                    request.getSenderAccountId(),
                    accountType.CHECKING,
                    request.getSenderKey());
            senderWallet.setBalance(senderInitialBalance);

            Wallet receiverWallet = WalletFixture.createWallet(
                    request.getReceiverAccountId(),
                    accountType.CHECKING,
                    request.getReceiverKey());

            PixKey receiverKey = PixKeyFixture.createPixKey(
                    request.getReceiverKey(),
                    keyType.CPF,
                    request.getReceiverAccountId());

            PixKey senderKey = PixKeyFixture.createPixKey(
                    request.getSenderKey(),
                    keyType.CPF,
                    request.getSenderAccountId());

            PixTransfer pixTransfer = PixTransferFixture.createPixTransfer(request, senderKey, receiverKey);

            when(walletService.getWalletById(request.getSenderAccountId()))
                    .thenReturn(Optional.of(senderWallet));
            when(walletService.getWalletById(request.getReceiverAccountId()))
                    .thenReturn(Optional.of(receiverWallet));

            // Act & Assert
            InsufficientBalanceException exception = assertThrows(
                    InsufficientBalanceException.class,
                    () -> transferExecution.transferExecutionWithRetry(pixTransfer)
            );

            assertEquals("Insufficient funds in sender's wallet", exception.getMessage());

            verify(walletService, never()).saveWallet(any(Wallet.class));
            verify(ledgerService, never()).registryLedgeEntries(any());
        }

        @Test
        @DisplayName("Should fail when sender wallet does not exist")
        public void shouldFail_WhenSenderWalletDoesNotExists() {

            BigDecimal amount = new BigDecimal("100");

            TransactionDTO request = TransactionDTOFixture.createTransactionDTO(amount);

            PixKey senderKey = PixKeyFixture.createPixKey(
                    request.getSenderKey(),
                    keyType.CPF,
                    request.getSenderAccountId());

            PixKey receiverKey = PixKeyFixture.createPixKey(
                    request.getReceiverKey(),
                    keyType.CPF,
                    request.getReceiverAccountId());

            when(walletService.getWalletById(request.getSenderAccountId()))
                    .thenReturn(Optional.empty());

            PixTransfer pixTransfer = PixTransferFixture.createPixTransfer(request, senderKey, receiverKey);

            // Act & Assert
            WalletNotFoundException exception = assertThrows(
                    WalletNotFoundException.class,
                    () -> transferExecution.transferExecutionWithRetry(pixTransfer)
            );

            assertEquals("Sender wallet not found.", exception.getMessage());

            verify(walletService, times(1)).getWalletById(request.getSenderAccountId());
            verify(walletService, never()).saveWallet(any(Wallet.class));
            verify(ledgerService, never()).registryLedgeEntries(any(PixTransfer.class));
        }

        @Test
        @DisplayName("Should fail when receiver wallet does not exist")
        public void shouldFail_WhenReceiverWalletDoesNotExists() {

            BigDecimal amount = new BigDecimal("100");

            TransactionDTO request = TransactionDTOFixture.createTransactionDTO(amount);

            Wallet senderWallet = WalletFixture.createWallet(
                    request.getSenderAccountId(),
                    accountType.CHECKING,
                    request.getSenderKey());

            PixKey senderKey = PixKeyFixture.createPixKey(
                    request.getSenderKey(),
                    keyType.CPF,
                    request.getSenderAccountId());

            PixKey receiverKey = PixKeyFixture.createPixKey(
                    request.getReceiverKey(),
                    keyType.CPF,
                    request.getReceiverAccountId());

            when(walletService.getWalletById(request.getSenderAccountId()))
                    .thenReturn(Optional.of(senderWallet));

            when(walletService.getWalletById(request.getReceiverAccountId()))
                    .thenReturn(Optional.empty());

            PixTransfer pixTransfer = PixTransferFixture.createPixTransfer(request, senderKey, receiverKey);

            // Act & Assert
            WalletNotFoundException exception = assertThrows(
                    WalletNotFoundException.class,
                    () -> transferExecution.transferExecutionWithRetry(pixTransfer)
            );

            assertEquals("Receiver wallet not found.", exception.getMessage());

            assertNotNull(exception);
            assertEquals(TransferResult.Status.RECEIVER_WALLET_NOT_FOUND.toString(), exception.getErrorCode());
            assertEquals("Receiver wallet not found.", exception.getMessage());


            verify(walletService, times(1)).getWalletById(request.getSenderAccountId());
            verify(walletService, times(1)).getWalletById(request.getReceiverAccountId());
            verify(walletService, never()).saveWallet(any(Wallet.class));
            verify(ledgerService, never()).registryLedgeEntries(any(PixTransfer.class));
        }

        @Test
        @DisplayName("Should fail when LedgerService throws FailedToSaveLedgeEntry exception")
        public void shouldFail_WhenLedgerServiceFails() {
            BigDecimal amount = new BigDecimal("100");
            BigDecimal senderInitialBalance = new BigDecimal("1000");

            TransactionDTO request = TransactionDTOFixture.createTransactionDTO(amount);

            Wallet senderWallet = WalletFixture.createWallet(
                    request.getSenderAccountId(),
                    accountType.CHECKING,
                    request.getSenderKey());
            senderWallet.setBalance(senderInitialBalance);

            Wallet receiverWallet = WalletFixture.createWallet(
                    request.getReceiverAccountId(),
                    accountType.CHECKING,
                    request.getReceiverKey());

            PixKey receiverKey = PixKeyFixture.createPixKey(
                    request.getReceiverKey(),
                    keyType.CPF,
                    request.getReceiverAccountId());

            PixKey senderKey = PixKeyFixture.createPixKey(
                    request.getSenderKey(),
                    keyType.CPF,
                    request.getSenderAccountId());

            PixTransfer pixTransfer = PixTransferFixture.createPixTransfer(request, senderKey, receiverKey);

            when(walletService.getWalletById(request.getSenderAccountId()))
                    .thenReturn(Optional.of(senderWallet));
            when(walletService.getWalletById(request.getReceiverAccountId()))
                    .thenReturn(Optional.of(receiverWallet));

            doThrow(new FailedToSaveLedgerEntry(request.getTransactionId()))
                    .when(ledgerService).registryLedgeEntries(any(PixTransfer.class));

            // Act & Assert
            FailedToSaveLedgerEntry exception = assertThrows(
                    FailedToSaveLedgerEntry.class,
                    () -> transferExecution.transferExecutionWithRetry(pixTransfer)
            );

            assertEquals("Failed to save ledger entry for transaction: " + request.getTransactionId(), exception.getMessage());

            verify(walletService, times(1)).getWalletById(request.getSenderAccountId());
            verify(walletService, times(1)).getWalletById(request.getReceiverAccountId());
            verify(walletService, never()).saveWallet(any(Wallet.class));
            verify(ledgerService, times(1)).registryLedgeEntries(any(PixTransfer.class));
        }

        @Test
        @DisplayName("Should fail when balance is zero")
        public void shouldFail_WhenBalanceIsZero() {

            BigDecimal amount = new BigDecimal("0");
            BigDecimal senderInitialBalance = new BigDecimal("10");

            TransactionDTO request = TransactionDTOFixture.createTransactionDTO(amount);

            Wallet senderWallet = WalletFixture.createWallet(
                    request.getSenderAccountId(),
                    accountType.CHECKING,
                    request.getSenderKey());
            senderWallet.setBalance(senderInitialBalance);

            Wallet receiverWallet = WalletFixture.createWallet(
                    request.getReceiverAccountId(),
                    accountType.CHECKING,
                    request.getReceiverKey());

            PixKey receiverKey = PixKeyFixture.createPixKey(
                    request.getReceiverKey(),
                    keyType.CPF,
                    request.getReceiverAccountId());

            PixKey senderKey = PixKeyFixture.createPixKey(
                    request.getSenderKey(),
                    keyType.CPF,
                    request.getSenderAccountId());

            PixTransfer pixTransfer = PixTransferFixture.createPixTransfer(request, senderKey, receiverKey);

            when(walletService.getWalletById(request.getSenderAccountId()))
                    .thenReturn(Optional.of(senderWallet));
            when(walletService.getWalletById(request.getReceiverAccountId()))
                    .thenReturn(Optional.of(receiverWallet));

            // Act & Assert
            InvalidAmountException exception = assertThrows(
                    InvalidAmountException.class,
                    () -> transferExecution.transferExecutionWithRetry(pixTransfer)
            );

            assertEquals(InvalidAmountException.ERROR_MESSAGE, exception.getMessage());
            assertEquals(InvalidAmountException.CODE, exception.getErrorCode());

            verify(walletService, never()).saveWallet(any(Wallet.class));
            verify(ledgerService, never()).registryLedgeEntries(any());
        }

        @Test
        @DisplayName("Should fail when balance is nagative")
        public void shouldFail_WhenTransferAmountIsNegative() {

            BigDecimal amount = new BigDecimal("-1");
            BigDecimal senderInitialBalance = new BigDecimal("10");

            TransactionDTO request = TransactionDTOFixture.createTransactionDTO(amount);

            Wallet senderWallet = WalletFixture.createWallet(
                    request.getSenderAccountId(),
                    accountType.CHECKING,
                    request.getSenderKey());
            senderWallet.setBalance(senderInitialBalance);

            Wallet receiverWallet = WalletFixture.createWallet(
                    request.getReceiverAccountId(),
                    accountType.CHECKING,
                    request.getReceiverKey());

            PixKey receiverKey = PixKeyFixture.createPixKey(
                    request.getReceiverKey(),
                    keyType.CPF,
                    request.getReceiverAccountId());

            PixKey senderKey = PixKeyFixture.createPixKey(
                    request.getSenderKey(),
                    keyType.CPF,
                    request.getSenderAccountId());

            PixTransfer pixTransfer = PixTransferFixture.createPixTransfer(request, senderKey, receiverKey);

            when(walletService.getWalletById(request.getSenderAccountId()))
                    .thenReturn(Optional.of(senderWallet));
            when(walletService.getWalletById(request.getReceiverAccountId()))
                    .thenReturn(Optional.of(receiverWallet));

            // Act & Assert
            InvalidAmountException exception = assertThrows(
                    InvalidAmountException.class,
                    () -> transferExecution.transferExecutionWithRetry(pixTransfer)
            );

            assertEquals(InvalidAmountException.ERROR_MESSAGE, exception.getMessage());
            assertEquals(InvalidAmountException.CODE, exception.getErrorCode());

            verify(walletService, never()).saveWallet(any(Wallet.class));
            verify(ledgerService, never()).registryLedgeEntries(any());
        }
    }
}
