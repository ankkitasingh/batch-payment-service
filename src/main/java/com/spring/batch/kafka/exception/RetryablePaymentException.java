package com.spring.batch.kafka.exception;

public class RetryablePaymentException extends RuntimeException {
    public RetryablePaymentException(String msg) { super(msg); }
    public RetryablePaymentException(String msg, Throwable t) { super(msg, t); }
}

