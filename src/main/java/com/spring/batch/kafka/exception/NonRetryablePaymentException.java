package com.spring.batch.kafka.exception;

public class NonRetryablePaymentException extends RuntimeException {
    public NonRetryablePaymentException(String msg) { super(msg); }
}
