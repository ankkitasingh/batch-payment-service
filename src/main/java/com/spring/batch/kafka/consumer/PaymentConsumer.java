package com.spring.batch.kafka.consumer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;


import com.spring.batch.kafka.model.PaymentCommand;
import com.spring.batch.kafka.service.PaymentProcessingService;


@Service

public class PaymentConsumer {
	
	private static final Logger log =
            LoggerFactory.getLogger(PaymentConsumer.class);

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
    	
    	log.info("Received batch of {} messages", batch.size());

        for (PaymentCommand payment : batch) {
        	log.info("Processing payment {}", payment.paymentId());
            service.process(payment);
        }
        log.info("Acknowledging batch");
        ack.acknowledge();
    }
}
