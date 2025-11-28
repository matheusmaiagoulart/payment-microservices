package com.matheus.payments.instant.Application.Facades;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.instant.Application.DTOs.Response.PaymentProcessorResponse;
import com.matheus.payments.instant.Application.DTOs.Request.TransactionRequest;
import com.matheus.payments.instant.Application.Services.OutboxService;
import com.matheus.payments.instant.Application.Services.PaymentProcessorService;
import com.matheus.payments.instant.Application.Services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Service;
import java.io.IOException;


@Slf4j
@Service
public class InstantPaymentFacade {

    private final TransactionService transactionService;
    private final PaymentProcessorService paymentProcessorService;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    public InstantPaymentFacade
            (ObjectMapper objectMapper,
             OutboxService outboxService,
             TransactionService transactionService,
             PaymentProcessorService paymentProcessorService)
    {
        this.objectMapper = objectMapper;
        this.outboxService = outboxService;
        this.transactionService = transactionService;
        this.paymentProcessorService = paymentProcessorService;
    }

    public PaymentProcessorResponse paymentOrchestration(TransactionRequest request) throws IOException {

        // Create the payment process to generate transactionID
        String transactionId = transactionService.createPaymentProcess(request);

        // Create Outbox Entry
        outboxService.createOutboxEntry(transactionId, objectMapper.writeValueAsString(request));

        log.info("Payment processing started",
                LogBuilder.serviceLog("/transaction/pix", "Payment", transactionId, "PaymentService", "paymentOrchestration", "payment.processing.started"));

        // Send payment to processor (Wallet Server)
        String processorResponseJson = paymentProcessorService.sendPaymentToProcessor(transactionId);

        // Convert JSON response to PaymentProcessorResponse object
        PaymentProcessorResponse processorResponse = objectMapper.readValue(processorResponseJson, PaymentProcessorResponse.class);

        // Update payment status based on processor response
        return paymentProcessorService.paymentStatusUpdate(processorResponse);
    }


}



