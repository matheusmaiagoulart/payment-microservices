package com.matheus.payments.Application.UseCases;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.Application.Audit.InstantPaymentFacadeAudit;
import com.matheus.payments.Application.DTOs.TransactionRequest;
import com.matheus.payments.Application.Services.PaymentProcessorService;
import com.matheus.payments.Application.Services.TransactionIdempotencyService;
import com.matheus.payments.Application.Services.TransactionService;

import org.shared.DTOs.PaymentProcessorResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Facade Class is responsible for orchestrating the instant payment process.
 * It integrates various services to handle with workflow process
 *
 * @author Matheus Maia Goulart
 */
@Service
public class InstantPayment {


    private final ObjectMapper objectMapper;
    private final TransactionIdempotencyService idempotencyService;
    private final InstantPaymentFacadeAudit audit;
    private final TransactionService transactionService;
    private final PaymentProcessorService paymentProcessorService;

    public InstantPayment
            (ObjectMapper objectMapper,
             TransactionIdempotencyService idempotencyService,
             InstantPaymentFacadeAudit audit,
             TransactionService transactionService,
             PaymentProcessorService paymentProcessorService) {
        this.audit = audit;
        this.objectMapper = objectMapper;
        this.idempotencyService = idempotencyService;
        this.transactionService = transactionService;
        this.paymentProcessorService = paymentProcessorService;
    }
    
    public PaymentProcessorResponse paymentOrchestration(TransactionRequest request) throws IOException {

        String transactionId = initializeTransaction(request);

        PaymentProcessorResponse response = paymentProcessorService.sendPaymentToProcessor(transactionId);

        // Update payment status based on processor response
        return paymentProcessorService.paymentStatusUpdate(response);
    }

    private String initializeTransaction(TransactionRequest request) throws IOException {
        String transactionId = transactionService.createPaymentProcess(request);
        request.setTransactionId(transactionId);
        idempotencyService.createTransactionIdempotencyEntry(transactionId, objectMapper.writeValueAsString(request));
        audit.logPaymentProcessStarting(transactionId); // LOG
        return transactionId;
    }
}



