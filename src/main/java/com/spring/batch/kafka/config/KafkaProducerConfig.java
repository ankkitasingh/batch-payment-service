package com.spring.batch.kafka.config;

import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.spring.batch.kafka.model.PaymentCommand;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, PaymentCommand> producerFactory(
            KafkaProperties properties) {

        DefaultKafkaProducerFactory<String, PaymentCommand> factory =
                new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());

        factory.setTransactionIdPrefix("payment-tx-");

        return factory;
    }

    @Bean
    public KafkaTemplate<String, PaymentCommand> kafkaTemplate(
            ProducerFactory<String, PaymentCommand> pf) {
        return new KafkaTemplate<>(pf);
    }
}
