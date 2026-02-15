package com.matheus.payments.wallet.UnitTests.Application.Services;

import com.matheus.payments.wallet.Application.Audit.LedgerAudit;
import com.matheus.payments.wallet.Application.DTOs.Context.PixTransfer;
import com.matheus.payments.wallet.Application.Services.LedgerService;
import com.matheus.payments.wallet.Domain.Models.WalletLedger;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.FailedToSaveLedgeEntry;
import com.matheus.payments.wallet.Infra.Repository.WalletLedgeRepository;
import com.matheus.payments.wallet.UnitTests.Fixtures.PixKeyFixture;
import com.matheus.payments.wallet.UnitTests.Fixtures.PixTransferFixture;
import com.matheus.payments.wallet.UnitTests.Fixtures.TransactionDTOFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shared.DTOs.TransactionDTO;
import org.shared.Domain.keyType;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LedgerServiceTests {

    @Mock
    private LedgerAudit audit;

    @Mock
    private WalletLedgeRepository walletLedgeRepository;

    @InjectMocks
    private LedgerService ledgerService;

    private PixTransfer createValidPixTransfer() {
        TransactionDTO request = TransactionDTOFixture.createTransactionDTO(new BigDecimal("10.00"));
        return PixTransferFixture.createPixTransfer(
                request,
                PixKeyFixture.createPixKey(request.getSenderKey(), keyType.CPF, request.getSenderAccountId()),
                PixKeyFixture.createPixKey(request.getReceiverKey(), keyType.CPF, request.getReceiverAccountId())
        );
    }

    @Nested
    @DisplayName("SUCCESS SCENARIOS")
    class SuccessScenarios {

        @Test
        @DisplayName("Should register ledger entries successfully")
        public void shouldRegisterLedgerEntriesSuccessfully() {
            // Arrange
            PixTransfer pixTransfer = createValidPixTransfer();
            when(walletLedgeRepository.saveAndFlush(any(WalletLedger.class))).thenReturn(new WalletLedger());

            // Act
            assertDoesNotThrow(() -> ledgerService.registryLedgeEntries(pixTransfer));

            // Assert
            ArgumentCaptor<WalletLedger> ledgerCaptor = ArgumentCaptor.forClass(WalletLedger.class);
            verify(walletLedgeRepository, times(2)).saveAndFlush(ledgerCaptor.capture());

            var capturedLedgers = ledgerCaptor.getAllValues();
            assertEquals(2, capturedLedgers.size());

            // Verify debit entry
            WalletLedger debitEntry = capturedLedgers.get(0);
            assertEquals(WalletLedger.EntryType.DEBIT, debitEntry.getEntryType());
            assertEquals(pixTransfer.getSenderPixKey().getAccountId(), debitEntry.getWalletId());
            assertEquals(pixTransfer.getReceiverPixKey().getAccountId(), debitEntry.getCounterpartyWalletId());
            assertEquals(pixTransfer.getAmount(), debitEntry.getAmount());

            // Verify credit entry
            WalletLedger creditEntry = capturedLedgers.get(1);
            assertEquals(WalletLedger.EntryType.CREDIT, creditEntry.getEntryType());
            assertEquals(pixTransfer.getReceiverPixKey().getAccountId(), creditEntry.getWalletId());
            assertEquals(pixTransfer.getSenderPixKey().getAccountId(), creditEntry.getCounterpartyWalletId());
            assertEquals(pixTransfer.getAmount(), creditEntry.getAmount());

            verifyNoInteractions(audit);
        }
    }

    @Nested
    @DisplayName("FAILURE SCENARIOS")
    class FailureScenarios {

        @Test
        @DisplayName("Should throw FailedToSaveLedgeEntry when DataIntegrityViolationException occurs")
        public void shouldThrowFailedToSaveLedgeEntry_WhenDataIntegrityViolationOccurs() {
            // Arrange
            PixTransfer pixTransfer = createValidPixTransfer();
            when(walletLedgeRepository.saveAndFlush(any(WalletLedger.class)))
                    .thenThrow(new DataIntegrityViolationException("Database constraint violation"));

            // Act & Assert
            FailedToSaveLedgeEntry exception = assertThrows(
                    FailedToSaveLedgeEntry.class,
                    () -> ledgerService.registryLedgeEntries(pixTransfer)
            );

            assertNotNull(exception);
            assertEquals(FailedToSaveLedgeEntry.class, exception.getClass());
            verify(audit, times(1)).logFailedCreateLedgerEntries(
                    pixTransfer.getTransactionId().toString(),
                    pixTransfer.getSenderPixKey().getKeyValue()
            );
            verify(walletLedgeRepository, times(1)).saveAndFlush(any(WalletLedger.class));
        }

        @Test
        @DisplayName("Should log audit and throw exception when debit entry save fails")
        public void shouldLogAuditAndThrowException_WhenDebitEntrySaveFails() {
            // Arrange
            PixTransfer pixTransfer = createValidPixTransfer();
            doThrow(new DataIntegrityViolationException("Duplicate entry"))
                    .when(walletLedgeRepository).saveAndFlush(any(WalletLedger.class));

            // Act & Assert
            assertThrows(FailedToSaveLedgeEntry.class, () -> ledgerService.registryLedgeEntries(pixTransfer));

            verify(audit, times(1)).logFailedCreateLedgerEntries(anyString(), anyString());
        }
    }
}

