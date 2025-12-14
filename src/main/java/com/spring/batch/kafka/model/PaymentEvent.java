package com.spring.batch.kafka.model;

public record PaymentEvent(
        String paymentId,
        String status
) {}