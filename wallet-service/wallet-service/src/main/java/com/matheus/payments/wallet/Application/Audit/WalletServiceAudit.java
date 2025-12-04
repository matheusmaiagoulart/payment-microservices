package com.matheus.payments.wallet.Application.Audit;

import lombok.extern.slf4j.Slf4j;
import org.shared.Logs.LogBuilder;
import org.springframework.stereotype.Component;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class WalletServiceAudit {

    private final String CLASS_NAME = "WalletService";
    private final String METHOD_NAME = "transferProcess";
    private final String MICROSERVICE_NAME = "wallet-service";


    public void logStartingTransferProcess(String transactionId) {
        log.info("Starting transfer process",
                LogBuilder.serviceLog("/wallets/instant-payment", MICROSERVICE_NAME, transactionId, CLASS_NAME, METHOD_NAME,
                        "transfer.process.starting"));
    }

    public void logBalanceValidation(String transactionId) {
        log.info("Validating balance",
                LogBuilder.serviceLog("/wallets/instant-payment", MICROSERVICE_NAME, transactionId, CLASS_NAME, METHOD_NAME,
                        "transfer.process.balance.validation"));
    }

    public void logTransferSuccess(String transactionId) {
        log.info("Transfer completed successfully",
                LogBuilder.serviceLog("/wallets/instant-payment", MICROSERVICE_NAME, transactionId, CLASS_NAME, METHOD_NAME,
                        "transfer.process.success"));
    }

    public void logTransferError(String transactionId, String errorMessage) {
        log.info("Transfer failed",
                LogBuilder.serviceLog("/wallets/instant-payment", MICROSERVICE_NAME, transactionId, CLASS_NAME, METHOD_NAME,
                        "transfer.process.failed"),
                kv("errorMessage", errorMessage));
    }

    public void logAlreadyProcessed(String transactionId) {
        log.info("Transfer already processed by Transaction ID",
                LogBuilder.serviceLog("/wallets/instant-payment", MICROSERVICE_NAME, transactionId, CLASS_NAME, METHOD_NAME,
                        "transfer.process.failed"));
    }



}
