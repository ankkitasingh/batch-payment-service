package com.spring.batch.kafka.consumer;

import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.spring.batch.kafka.model.PaymentCommand;
import com.spring.batch.kafka.service.PaymentProcessingService;

@Service
public class PaymentConsumer {

	private final PaymentProcessingService service;

    public PaymentConsumer(PaymentProcessingService service) {
        this.service = service;
    }

    @KafkaListener(
        topics = "payment-commands",
        containerFactory = "batchFactory"
    )
    public void consume(List<PaymentCommand> batch,
                        Acknowledgment ack) {

        for (PaymentCommand payment : batch) {
            service.process(payment);
        }

        ack.acknowledge();
    }
}
