package com.matheus.payments.instant.Controller;

import com.matheus.payments.instant.Application.DTOs.Response.PaymentProcessorResponse;
import com.matheus.payments.instant.Application.DTOs.Request.TransactionRequest;
import com.matheus.payments.instant.Application.Services.InstantPaymentServices.PaymentService;
import com.matheus.payments.instant.Application.Services.StatementService;
import com.matheus.payments.instant.Domain.Transaction.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private StatementService statementService;

    @PostMapping("/pix")
    public ResponseEntity<PaymentProcessorResponse> CreateInstantPayment(@RequestBody TransactionRequest request) throws IOException, InterruptedException {

        PaymentProcessorResponse paymentOrchestration = paymentService.paymentOrchestration(request);

            return ResponseEntity.ok(paymentOrchestration);
    }

    @GetMapping("/account-statement")
    public ResponseEntity<List<Transaction>> instantPayment(@RequestParam (name = "account", required = true)UUID account) {
        return ResponseEntity.ok(statementService.getAllTransactionsStatements(account));
    }
}
