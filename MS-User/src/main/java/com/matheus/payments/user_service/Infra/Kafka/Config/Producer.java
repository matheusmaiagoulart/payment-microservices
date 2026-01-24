package com.matheus.payments.user_service.Infra.Kafka.Config;

import com.matheus.payments.user_service.Utils.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;
@Configuration
@EnableKafka
public class Producer {

    private final KafkaProperties kafkaProperties;

    public Producer(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory()
    {
        Map<String, Object> properties = kafkaProperties .buildProducerProperties();

        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
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
    public NewTopic UserCreated() {
        return TopicBuilder
                .name(KafkaTopics.USER_CREATED)
                .partitions(1) //numero de consumidores que podem ler o topico em paralelo
                .replicas(1)
                .config("retention.ms", "604800000") //7 dias em milisegundos
                .build();
    }
}
