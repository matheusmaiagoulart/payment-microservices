package com.matheus.payments.Application.Services;

import com.matheus.payments.Domain.Events.DepositCreatedEvent;
import com.matheus.payments.Domain.Exceptions.DepositNotFound;
import com.matheus.payments.Domain.Models.Deposit;
import com.matheus.payments.Domain.Repositories.DepositRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DepositService {

    private final DepositRepository depositRepository;
    private final ApplicationEventPublisher eventPublisher;

    public DepositService(DepositRepository depositRepository, ApplicationEventPublisher eventPublisher) {
        this.depositRepository = depositRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void createDeposit(Deposit deposit) {
        depositRepository.saveDeposit(deposit);
        eventPublisher.publishEvent(new DepositCreatedEvent(deposit));
    }

    public void updateDeposit(Deposit deposit) {
        depositRepository.saveDeposit(deposit);
    }

    @Transactional
    public void setDepositStatusSent(String depositId) {
        Deposit deposit = depositRepository.getDepositById(UUID.fromString(depositId))
                .orElseThrow(() -> new DepositNotFound(depositId));

        deposit.markAsSent();
        updateDeposit(deposit);
    }

    @Transactional
    public void setDepositStatusExecuted(String depositId) {
        Deposit deposit = depositRepository.getDepositById(UUID.fromString(depositId))
                .orElseThrow(() -> new DepositNotFound(depositId));

        deposit.markAsConfirmed();
        updateDeposit(deposit);
    }

    @Transactional
    public void setDepositStatusFailed(String depositId) {
        Deposit deposit = depositRepository.getDepositById(UUID.fromString(depositId))
                .orElseThrow(() -> new DepositNotFound(depositId));

        deposit.markAsFailed();
        updateDeposit(deposit);
    }

}
