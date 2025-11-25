package com.matheus.payments.instant.Application.Services.InstantPaymentServices;


import com.matheus.payments.instant.Application.DTOs.Response.PaymentProcessorResponse;
import com.matheus.payments.instant.Application.DTOs.Request.TransactionRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
public class PaymentService {


    private final TransactionService transactionService;
    private final PaymentProcessorService paymentProcessorService;

    public PaymentService(TransactionService transactionService, PaymentProcessorService paymentProcessorService) {
        this.transactionService = transactionService;
        this.paymentProcessorService = paymentProcessorService;
    }

    public PaymentProcessorResponse paymentOrchestration(TransactionRequest request) throws IOException, InterruptedException {

        String transactionId = transactionService.createPaymentProcess(request);
        System.out.println("Enviando para o processador de pagamentos: " + transactionId);
        String processorResponse = paymentProcessorService.sendPaymentToProcessor(transactionId);
        return paymentProcessorService.paymentStatusUpdate(processorResponse);
    }


}



