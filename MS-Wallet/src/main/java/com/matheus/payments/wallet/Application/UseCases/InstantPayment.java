package com.matheus.payments.wallet.Application.UseCases;

import com.matheus.payments.wallet.Application.Audit.WalletServiceAudit;
import com.matheus.payments.wallet.Application.DTOs.Context.PixTransfer;
import com.matheus.payments.wallet.Application.DTOs.Response.InstantPaymentResponse;
import com.matheus.payments.wallet.Application.Services.PixKeyService;
import com.matheus.payments.wallet.Application.Services.TransferExecution;
import com.matheus.payments.wallet.Domain.Exceptions.*;
import com.matheus.payments.wallet.Domain.Models.TransactionsProcessed;
import com.matheus.payments.wallet.Domain.Models.PixKey;
import com.matheus.payments.wallet.Infra.Exceptions.Custom.*;
import com.matheus.payments.wallet.Domain.Repositories.TransactionProcessedRepository;
import org.shared.DTOs.TransactionDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InstantPayment {

    private final WalletServiceAudit audit;
    private final PixKeyService pixKeyService;
    private final TransferExecution transferExecution;
    private final TransactionProcessedRepository transactionsProcessedRepository;

    public InstantPayment(PixKeyService pixKeyService, WalletServiceAudit audit, TransactionProcessedRepository transactionsProcessedRepository, TransferExecution transferExecution) {
        this.audit = audit;
        this.pixKeyService = pixKeyService;
        this.transferExecution = transferExecution;
        this.transactionsProcessedRepository = transactionsProcessedRepository;
    }


    @Transactional
    public InstantPaymentResponse transferProcess(TransactionDTO request) {

        audit.logStartingTransferProcess(request.getTransactionId()); // LOG
        PixTransfer pixTransfer = new PixTransfer(request.getTransactionId(), null, null, request.getAmount());

        try {
            pixTransfer = createPixTransfer(request);
            saveProcessedTransaction(pixTransfer.getTransactionId());

            sameUserValidation(pixTransfer.getSenderPixKey().getAccountId(), pixTransfer.getReceiverPixKey().getAccountId());

            transferExecution.transferExecutionWithRetry(pixTransfer);

            return successTransfer(pixTransfer);
        }
        catch (FailedToSaveLedgerEntry e) {
            return failedTransfer(pixTransfer, "Failed to save ledger entries: " + e.getMessage());
        }
        catch (OptimisticLockingFailureException e) {
            audit.logFailedGeneric(pixTransfer.getTransactionId().toString(), "Max retry attempts reached for transaction due to concurrent updates.");
            return failedTransfer(pixTransfer, "Concurrent transaction conflict");
        }
        catch (TransactionAlreadyProcessed e) {
            return idempotencyError(pixTransfer, e);
        }
        catch (DomainException e) {
            return failedTransfer(pixTransfer, e.getMessage());
        }
    }

    /**
     * Save transactionId to processed transactions for idempotency control.
     *
     * @param transactionId
     * @throws TransactionAlreadyProcessed If transaction was already processed
     */
    private void saveProcessedTransaction(UUID transactionId) {
        try {
            transactionsProcessedRepository.saveAndFlush(new TransactionsProcessed(transactionId));
        } catch (DataIntegrityViolationException e) {
            throw new TransactionAlreadyProcessed();
        }
    }

    private void sameUserValidation(UUID senderWalletId, UUID receiverWalletId) {
        // Same user validation
        if (senderWalletId.equals(receiverWalletId)) {
            throw new SameUserException("Sender and Receiver cannot be the same");
        }
    }

    /**
     * This method populate the PixTransfer, who holds all necessary information for the transfer process.
     *
     * @param request All data from the transfer request
     */
    private PixTransfer createPixTransfer(TransactionDTO request) {
        PixKey accountIdSender = pixKeyService.getWalletIdByKey(request.getSenderKey()).orElseThrow(() -> WalletNotFoundException.senderNotFound());
        PixKey accountIdReceiver = pixKeyService.getWalletIdByKey(request.getReceiverKey()).orElseThrow(() -> WalletNotFoundException.receiverNotFound());

        return new PixTransfer(request.getTransactionId(), accountIdSender, accountIdReceiver, request.getAmount());
    }

    /**
     * Methods to build different response types, according to transfer process result
     *
     * @param pixTransfer
     */
    private InstantPaymentResponse successTransfer(PixTransfer pixTransfer) {
        audit.logTransferSuccess(pixTransfer.getTransactionId().toString()); // LOG
        return new InstantPaymentResponse(true, false, pixTransfer.getSenderPixKey().getAccountId(), pixTransfer.getReceiverPixKey().getAccountId());
    }

    private InstantPaymentResponse idempotencyError(PixTransfer pixTransfer, TransactionAlreadyProcessed e) {
        audit.logTransferError(pixTransfer.getTransactionId().toString(), e.getMessage()); // LOG
        return new InstantPaymentResponse(true, true, pixTransfer.getSenderPixKey().getAccountId(), pixTransfer.getReceiverPixKey().getAccountId(), e.getMessage());
    }

    private InstantPaymentResponse failedTransfer(PixTransfer pixTransfer, String errorMessage) {
        audit.logTransferError(pixTransfer.getTransactionId().toString(), errorMessage); // LOG

        UUID senderId = pixTransfer.getSenderPixKey() != null ? pixTransfer.getSenderPixKey().getAccountId() : null;
        UUID receiverId = pixTransfer.getReceiverPixKey() != null ? pixTransfer.getReceiverPixKey().getAccountId() : null;

        return new InstantPaymentResponse(false, false, senderId, receiverId, errorMessage);
    }
}
