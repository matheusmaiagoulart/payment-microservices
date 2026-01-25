package com.matheus.payments.wallet.Api.Controller;

import com.matheus.payments.wallet.Application.Audit.CorrelationId;
import com.matheus.payments.wallet.Infra.Kafka.Listeners.UserCreated.UserCreatedEvent;
import com.matheus.payments.wallet.Application.DTOs.Response.InstantPaymentResponse;
import com.matheus.payments.wallet.Application.UseCases.CreateWallet;
import com.matheus.payments.wallet.Application.UseCases.InstantPayment;
import org.shared.DTOs.PaymentProcessorResponse;
import org.shared.DTOs.TransactionDTO;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final InstantPayment instantPayment;
    private final CreateWallet createWallet;

    public WalletController(InstantPayment instantPayment, CreateWallet createWallet) {
        this.instantPayment = instantPayment;
        this.createWallet = createWallet;
    }

    @PostMapping("/instant-payment")
    public PaymentProcessorResponse instantPayment(@RequestHeader("X-Correlation-Id") String correlationId, @RequestBody TransactionDTO request) {

        CorrelationId.set(correlationId);
        try {
            InstantPaymentResponse result = instantPayment.transferProcess(request);

            if (!result.isSucessful()) {
                return PaymentProcessorResponse.failedResponse(false, UUID.fromString(request.getTransactionId()), result.getSenderAccountId(), result.getReceiverAccountId(), result.getFailedMessage());
            } else if (result.isAlreadyProcessed()) {
                return PaymentProcessorResponse.failedResponse(true, UUID.fromString(request.getTransactionId()), result.getSenderAccountId(), result.getReceiverAccountId(), result.getFailedMessage());
            }

            return PaymentProcessorResponse.successResponse(UUID.fromString(request.getTransactionId()), result.getSenderAccountId(), result.getReceiverAccountId());

        } finally {
            CorrelationId.clear();
        }
    }
}