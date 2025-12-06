package com.matheus.payments.wallet.Application.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.payments.wallet.Application.DTOs.Response.InstantPaymentResponse;
import com.matheus.payments.wallet.utils.KafkaTopics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.shared.DTOs.TransactionDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class PaymentProcessorService {

    private ObjectMapper objectMapper;
    private WalletService walletService;
    private KafkaTemplate kafkaTemplate;
    public PaymentProcessorService(ObjectMapper objectMapper, WalletService walletService, KafkaTemplate kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.walletService = walletService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopics.INSTANT_PAYMENT_TOPIC, groupId = "wallet-service-group")
    public void processPaymentRequest(ConsumerRecord<String, String> message) throws JsonProcessingException {

        System.out.println("Received payment request from Kafka topic: " + KafkaTopics.INSTANT_PAYMENT_TOPIC);

        String json = message.value();
        String key = message.key();

        TransactionDTO request = objectMapper.readValue(json, TransactionDTO.class);
        System.out.println("Recebida resposta de pagamento: " + request);
        System.out.println("Key: " + key);


        InstantPaymentResponse result = walletService.transferProcess(request);

        String responseJson = objectMapper.writeValueAsString(result);
        kafkaTemplate.send(KafkaTopics.INSTANT_PAYMENT_TOPIC_RESPONSE, key, responseJson);
    }
}
