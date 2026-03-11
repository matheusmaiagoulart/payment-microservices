package com.matheus.payments.Application.UseCases;

import com.matheus.payments.Application.DTOs.DepositRequest;
import com.matheus.payments.Application.Services.DepositService;
import com.matheus.payments.Domain.Models.Deposit;
import org.springframework.stereotype.Service;


@Service
public class CashDeposit {

    private final DepositService depositService;

    public CashDeposit(DepositService depositService) {
        this.depositService = depositService;
    }

     public Deposit execute (DepositRequest depositRequest) {
        return depositService.createDeposit(depositRequest);
     }
}
