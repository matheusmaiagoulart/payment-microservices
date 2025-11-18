package com.matheus.payments.instant.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.matheus.payments.instant.Application.DTOs.Response.PaymentProcessorResponse;
import com.matheus.payments.instant.Application.DTOs.TransactionRequest;
import com.matheus.payments.instant.Application.Services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/pix")
    public ResponseEntity<PaymentProcessorResponse> CreateInstantPayment(@RequestBody TransactionRequest request) throws IOException, InterruptedException {

        PaymentProcessorResponse paymentOrchestration = paymentService.paymentOrchestration(request);

            return ResponseEntity.ok(paymentOrchestration);

    }

}
