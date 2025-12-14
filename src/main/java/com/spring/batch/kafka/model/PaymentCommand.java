package com.spring.batch.kafka.model;

public record PaymentCommand(
        String paymentId,
        String accountId,
        double amount,
        String currency
) {}
