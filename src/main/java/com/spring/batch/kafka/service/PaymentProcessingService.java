package com.spring.batch.kafka.service;

import org.springframework.stereotype.Service;

import com.spring.batch.kafka.enu.PaymentStatus;
import com.spring.batch.kafka.exception.NonRetryablePaymentException;
import com.spring.batch.kafka.exception.RetryablePaymentException;
import com.spring.batch.kafka.gateway.client.PaymentGatewayClient;
import com.spring.batch.kafka.ledger.LedgerService;
import com.spring.batch.kafka.model.PaymentCommand;
import com.spring.batch.kafka.repository.PaymentRepository;

@Service
public class PaymentProcessingService {

    private final PaymentRepository repository;
    private final PaymentGatewayClient gateway;
    private final LedgerService ledger;

    public PaymentProcessingService(
            PaymentRepository repository,
            PaymentGatewayClient gateway,
            LedgerService ledger) {

        this.repository = repository;
        this.gateway = gateway;
        this.ledger = ledger;
    }

    public void process(PaymentCommand payment) {

        if (payment.amount() <= 0) {
            throw new NonRetryablePaymentException("Invalid amount");
        }

        repository.find(payment.paymentId()).ifPresent(existing -> {
            if (existing == PaymentStatus.COMPLETED) return;
            throw new RetryablePaymentException("Already processing");
        });

        repository.save(payment.paymentId(), PaymentStatus.PROCESSING);

        try {
            gateway.charge(payment);
            ledger.record(payment);
            repository.save(payment.paymentId(), PaymentStatus.COMPLETED);
        } catch (Exception e) {
            repository.save(payment.paymentId(), PaymentStatus.FAILED);
            throw new RetryablePaymentException("Temporary failure", e);
        }
    }
}

