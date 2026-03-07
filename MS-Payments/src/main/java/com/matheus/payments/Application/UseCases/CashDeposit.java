package com.matheus.payments.Application.UseCases;

import com.matheus.payments.Application.DTOs.DepositRequest;
import com.matheus.payments.Application.Services.DepositService;
import com.matheus.payments.Domain.Models.Deposit;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;


@Service
public class CashDeposit {

    private final DepositService depositService;

    public CashDeposit(DepositService depositService) {
        this.depositService = depositService;
    }

     public void execute (DepositRequest depositRequest) {
        depositService.createDeposit(new Deposit(depositRequest.getSenderId(), depositRequest.getReceiverId(), depositRequest.getAmount()));
     }
}
