package com.matheus.payments.instant.Application.Services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.instant.Application.DTOs.TransactionRequest;
import com.matheus.payments.instant.Application.Mappers.Transaction.TransactionMapper;
import com.matheus.payments.instant.Domain.Transaction.Transaction;
import com.matheus.payments.instant.Domain.Transaction.TransactionOutbox;
import com.matheus.payments.instant.Infra.Repository.OutboxRepository;
import com.matheus.payments.instant.Infra.Repository.TransactionRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PaymentService {


    private TransactionMapper transactionMappers;
    private TransactionRepository transactionRepository;
    private OutboxRepository outboxRepository;
    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper;

    public PaymentService
            (
                    TransactionMapper transactionMappers,
                    TransactionRepository transactionRepository,
                    OutboxRepository outboxRepository,
                    KafkaTemplate<String, String> kafkaTemplate,
                    ObjectMapper objectMapper
            ) {
        this.transactionMappers = transactionMappers;
        this.transactionRepository = transactionRepository;
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    private final String PAYMENT_TOPIC = "instant-payments";

    @Transactional
    public String processPayment(TransactionRequest request) throws JsonProcessingException {

        System.out.println("Enviada solicitação de pagamento: " + request);

        // Create Transaction Entity
        Transaction transaction = transactionMappers.mapToEntity(request);

        // Create Outbox Entry
        TransactionOutbox outbox = new TransactionOutbox(transaction.getTransactionId().toString(), PAYMENT_TOPIC, objectMapper.writeValueAsString(transaction));
        outboxRepository.save(outbox);

        transactionRepository.save(transaction);

        return transaction.getTransactionId().toString();

    }


    public boolean sendPaymentToProcessor(String transactionId) {

        var transactionOutbox = outboxRepository.findByTransactionId(transactionId);
        if(transactionOutbox.isPresent()){

            TransactionOutbox outbox = transactionOutbox.get();

            if(!outbox.getSent()){
                // Send to Kafka
                kafkaTemplate.send(outbox.getTopic(), transactionId, outbox.getPayload());

                outbox.setSent(true);
                outboxRepository.save(outbox);
            }
            return true;
        }
        return false;
    }


}
