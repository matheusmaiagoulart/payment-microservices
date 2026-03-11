package com.matheus.payments.Controller;

import com.matheus.payments.Application.DTOs.DepositRequest;
import com.matheus.payments.Application.DTOs.DepositResponse;
import com.matheus.payments.Application.DTOs.TransactionRequest;
import com.matheus.payments.Application.UseCases.CashDeposit;
import com.matheus.payments.Application.UseCases.InstantPayment;
import com.matheus.payments.Application.Services.StatementService;
import com.matheus.payments.Domain.Models.Transaction;
import jakarta.validation.Valid;
import org.shared.DTOs.PaymentProcessorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transaction")
public class TransactionController {


    private final InstantPayment paymentService;
    private final StatementService statementService;
    private final CashDeposit cashDeposit;

    public TransactionController(InstantPayment paymentService, StatementService statementService, CashDeposit cashDeposit) {
        this.cashDeposit = cashDeposit;
        this.paymentService = paymentService;
        this.statementService = statementService;
    }

    @PostMapping("/pix")
    public ResponseEntity<PaymentProcessorResponse> createInstantPayment(@Valid @RequestBody TransactionRequest request) throws IOException {
        PaymentProcessorResponse paymentOrchestration = paymentService.paymentOrchestration(request);
        return ResponseEntity.status(HttpStatus.OK).body(paymentOrchestration);
    }

    @GetMapping("/account-statement")
    public ResponseEntity<List<Transaction>> accountStatement(@RequestParam (name = "account") UUID account) {
        return ResponseEntity.status(HttpStatus.OK).body(statementService.getAllTransactionsStatements(account));
    }

    @PostMapping("/deposit")
    public ResponseEntity<DepositResponse> createDeposit(@Valid @RequestBody DepositRequest request) {
        var deposit = cashDeposit.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new DepositResponse(deposit.getDepositId(), deposit.getStatus().toString()));
    }
}
