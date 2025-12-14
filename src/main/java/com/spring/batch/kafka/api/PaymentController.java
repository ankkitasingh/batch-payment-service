package com.spring.batch.kafka.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.batch.kafka.model.PaymentCommand;
import com.spring.batch.kafka.producer.PaymentProducer;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentProducer producer;

    public PaymentController(PaymentProducer producer) {
        this.producer = producer;
    }

    
    @PostMapping
    public ResponseEntity<String> pay(@RequestBody PaymentCommand command) {
        producer.send(command);
        return ResponseEntity.accepted().body("Payment accepted");
    }
}