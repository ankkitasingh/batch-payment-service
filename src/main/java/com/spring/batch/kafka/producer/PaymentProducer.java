package com.spring.batch.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


import com.spring.batch.kafka.model.PaymentCommand;

@Service
public class PaymentProducer {

    private final KafkaTemplate<String, PaymentCommand> kafkaTemplate;

    public PaymentProducer(KafkaTemplate<String, PaymentCommand> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    
    public void send(PaymentCommand command) {
        kafkaTemplate.send(
            "payment-commands",
            command.paymentId(),
            command
        );
    }
}

