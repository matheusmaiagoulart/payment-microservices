package com.matheus.payments.instant.Application.Facades;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.instant.Application.Audit.InstantPaymentFacadeAudit;
import com.matheus.payments.instant.Application.DTOs.Response.PaymentProcessorResponse;
import com.matheus.payments.instant.Application.DTOs.Request.TransactionRequest;
import com.matheus.payments.instant.Application.Services.OutboxService;
import com.matheus.payments.instant.Application.Services.PaymentProcessorService;
import com.matheus.payments.instant.Application.Services.TransactionService;

import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Facade Class is responsible for orchestrating the instant payment process.
 * It integrates various services to handle with workflow process
 *
 * @author Matheus Maia Goulart
 */
@Service
public class InstantPaymentFacade {


    private final ObjectMapper objectMapper;
    private final OutboxService outboxService;
    private final InstantPaymentFacadeAudit audit;
    private final TransactionService transactionService;
    private final PaymentProcessorService paymentProcessorService;

    public InstantPaymentFacade
            (ObjectMapper objectMapper,
             OutboxService outboxService,
             InstantPaymentFacadeAudit audit,
             TransactionService transactionService,
             PaymentProcessorService paymentProcessorService) {
        this.audit = audit;
        this.objectMapper = objectMapper;
        this.outboxService = outboxService;
        this.transactionService = transactionService;
        this.paymentProcessorService = paymentProcessorService;
    }

    public PaymentProcessorResponse paymentOrchestration(TransactionRequest request) throws IOException {

        // Create the payment process to generate transactionID
        String transactionId = transactionService.createPaymentProcess(request);
        request.setTransactionId(transactionId);

        // Create Outbox Entry
        outboxService.createOutboxEntry(transactionId, objectMapper.writeValueAsString(request));

        audit.logPaymentProcessStarting(transactionId); // LOG

        // Send payment to processor (Wallet Server)
        String processorResponseJson = paymentProcessorService.sendPaymentToProcessor(transactionId);

        // Convert JSON response to PaymentProcessorResponse object
        PaymentProcessorResponse processorResponse = objectMapper.readValue(processorResponseJson, PaymentProcessorResponse.class);

        // Update payment status based on processor response
        return paymentProcessorService.paymentStatusUpdate(processorResponse);
    }


}



