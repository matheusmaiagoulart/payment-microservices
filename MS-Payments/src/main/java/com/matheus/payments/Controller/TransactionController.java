package com.matheus.payments.Controller;

import com.matheus.payments.Application.DTOs.TransactionRequest;
import com.matheus.payments.Application.Facades.InstantPaymentFacade;
import com.matheus.payments.Application.Services.StatementService;
import com.matheus.payments.Domain.Transaction;
import jakarta.validation.Valid;
import org.shared.DTOs.PaymentProcessorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transaction")
public class TransactionController {


    private final InstantPaymentFacade paymentService;
    private final StatementService statementService;

    public TransactionController(InstantPaymentFacade paymentService, StatementService statementService) {
        this.paymentService = paymentService;
        this.statementService = statementService;
    }

    @PostMapping("/pix")
    public ResponseEntity<PaymentProcessorResponse> CreateInstantPayment(@Valid @RequestBody TransactionRequest request) throws IOException {
        PaymentProcessorResponse paymentOrchestration = paymentService.paymentOrchestration(request);
        return ResponseEntity.ok(paymentOrchestration);
    }

    @GetMapping("/account-statement")
    public ResponseEntity<List<Transaction>> AccountStatement(@RequestParam (name = "account") UUID account) {
        return ResponseEntity.ok(statementService.getAllTransactionsStatements(account));
    }
}
