package com.matheus.payments.wallet.Controller;

import com.matheus.payments.wallet.Application.Audit.CorrelationId;
import com.matheus.payments.wallet.Application.DTOs.Response.InstantPaymentResponse;
import com.matheus.payments.wallet.Application.UseCases.InstantPayment;
import org.shared.DTOs.PaymentProcessorResponse;
import org.shared.DTOs.TransactionDTO;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final InstantPayment instantPayment;

    public WalletController(InstantPayment instantPayment) {
        this.instantPayment = instantPayment;
    }

    @PostMapping("/instant-payment")
    public PaymentProcessorResponse instantPayment(@RequestHeader("X-Correlation-Id") String correlationId, @RequestBody TransactionDTO request) {
        CorrelationId.set(correlationId);

        try {
            InstantPaymentResponse result = instantPayment.transferProcess(request);

            if (!result.isSucessful()) {
                return PaymentProcessorResponse.failedResponse(result.isAlreadyProcessed(), UUID.fromString(request.getTransactionId()), result.getSenderAccountId(), result.getReceiverAccountId(), result.getFailedMessage());
            }
            return PaymentProcessorResponse.successResponse(UUID.fromString(request.getTransactionId()), result.getSenderAccountId(), result.getReceiverAccountId());

        } finally {
            CorrelationId.clear();
        }
    }
}