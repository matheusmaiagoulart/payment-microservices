package com.matheus.payments.instant.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.matheus.payments.instant.Application.DTOs.TransactionRequest;
import com.matheus.payments.instant.Application.Services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/pix")
    public String teste(@RequestBody TransactionRequest request) throws JsonProcessingException {

        String result = paymentService.processPayment(request);
        if(result != null){
            System.out.println("Enviando para o processador de pagamentos: " + result);
            var result2 = paymentService.sendPaymentToProcessor(result);
            if(result2){
                return "Payment sended to processor sucessfully";
            }
            return "Error sending payment to processor";
        }

        return "Error processing payment";
    }
}
