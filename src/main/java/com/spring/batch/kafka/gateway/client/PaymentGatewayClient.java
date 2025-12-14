package com.spring.batch.kafka.gateway.client;

import org.springframework.stereotype.Service;

import com.spring.batch.kafka.model.PaymentCommand;

@Service
public class PaymentGatewayClient {
    public void charge(PaymentCommand payment) {
        if (Math.random() < 0.2) {
            throw new RuntimeException("PSP timeout");
        }
    }
}
