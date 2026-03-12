package com.matheus.payments.wallet.Application.UseCases;

import com.matheus.payments.wallet.Application.Audit.DepositAudit;
import com.matheus.payments.wallet.Domain.Events.Deposit.DepositExecuted;
import com.matheus.payments.wallet.Domain.Events.Deposit.DepositFailed;
import com.matheus.payments.wallet.Application.Services.TransferExecution;
import com.matheus.payments.wallet.Domain.Exceptions.DepositAlreadyProcessed;
import com.matheus.payments.wallet.Domain.Exceptions.DomainException;
import com.matheus.payments.wallet.Domain.Models.DepositsProcessed;
import com.matheus.payments.wallet.Infra.Kafka.Listeners.DepositCreated.DepositCreated;
import com.matheus.payments.wallet.Domain.Repositories.DepositsProcessedRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class Deposit {

    private final DepositAudit audit;
    private final TransferExecution transferExecution;
    private final ApplicationEventPublisher publisher;
    private final DepositsProcessedRepository depositsProcessedRepository;

    public Deposit(DepositAudit audit, TransferExecution transferExecution, DepositsProcessedRepository depositsProcessedRepository, ApplicationEventPublisher publisher) {
        this.audit = audit;
        this.publisher = publisher;
        this.transferExecution = transferExecution;
        this.depositsProcessedRepository = depositsProcessedRepository;
    }

    @Transactional
    public void executeDeposit(DepositCreated deposit) throws DepositAlreadyProcessed {
        audit.logStartDepositExecution(deposit.getDepositId());

        try {
            saveProcessedDeposit(deposit.getDepositId());

            transferExecution.depositExecution(deposit);

            publishSuccess(deposit);

            audit.logDepositExecuted(deposit.getDepositId());

        } catch (DepositAlreadyProcessed e) {
            publishIdempotencyConflict(deposit);
        }
        catch (DomainException e) {
            publishDepositExecutionFailure(deposit, e.getMessage());
        }
        catch (Exception e) {
            // LOG
            publishDepositExecutionFailure(deposit, e.getMessage());
        }
    }

    /**
     * Save depositId to processed deposits for idempotency control.
     *
     * @throws DepositAlreadyProcessed If transaction was already processed
     */
    private void saveProcessedDeposit(UUID depositId) {
        try {
            depositsProcessedRepository.saveAndFlush(new DepositsProcessed(depositId));
        } catch (DataIntegrityViolationException e) {
            throw new DepositAlreadyProcessed();
        }
    }

    private void publishSuccess(DepositCreated deposit) {
        publisher.publishEvent(
                new DepositExecuted(deposit.getDepositId(), deposit.getReceiverId(), deposit.getAmount())
        );
    }

    private void publishDepositExecutionFailure(DepositCreated deposit, String failureReason) {
        log.error("Failed reason for DepositFailed Event for id: {}: {}", deposit.getDepositId(), failureReason);
        publisher.publishEvent(
                new DepositFailed(deposit.getDepositId(), false, deposit.getReceiverId(), deposit.getAmount(), failureReason));
    }

    private void publishIdempotencyConflict(DepositCreated deposit) {
        audit.logDepositFailed(deposit.getDepositId(),  DepositAlreadyProcessed.MESSAGE);
        publisher.publishEvent(
                new DepositFailed(deposit.getDepositId(), true, deposit.getReceiverId(), deposit.getAmount(), DepositAlreadyProcessed.MESSAGE));
    }
}
