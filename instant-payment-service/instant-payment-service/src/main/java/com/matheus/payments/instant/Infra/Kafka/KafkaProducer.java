package com.matheus.payments.instant.Infra.Kafka;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Value;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
@Configuration
@EnableKafka
public class KafkaProducer {


    private KafkaProperties kafkaProperties;
    public KafkaProducer(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    private final String pagamentoRequestTopic = "instant-payments";

    @Bean
    public ProducerFactory<String, String> producerFactory()
    {
        Map<String, Object> properties = kafkaProperties .buildProducerProperties();
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.ACKS_CONFIG, "all"); // garante que a mensagem seja confirmada por todos os réplicas
        properties.put(ProducerConfig.RETRIES_CONFIG, 3); // número de tentativas em caso de falha
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // garante que a mensagem seja entregue apenas uma vez
        properties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1); // uma mensagem por vez
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic pagamentoRequestTopicBuilder() {
        return TopicBuilder
                .name(pagamentoRequestTopic)
                .partitions(1) //numero de consumidores que podem ler o topico em paralelo
                .replicas(1)
                .config("retention.ms", "3600000") //1 hora em milisegundos
                .build();
    }



}
