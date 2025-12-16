package com.spring.batch.kafka.service;

import org.springframework.stereotype.Service;

import com.spring.batch.kafka.entity.Payments;
import com.spring.batch.kafka.enu.PaymentStatus;
import com.spring.batch.kafka.exception.NonRetryablePaymentException;
import com.spring.batch.kafka.exception.RetryablePaymentException;
import com.spring.batch.kafka.gateway.client.PaymentGatewayClient;
import com.spring.batch.kafka.ledger.LedgerService;
import com.spring.batch.kafka.model.PaymentCommand;
import com.spring.batch.kafka.repository.PaymentsRepository;

import jakarta.transaction.Transactional;

@Service
public class PaymentProcessingService {

	 	private final PaymentsRepository repository;
	    private final PaymentGatewayClient gateway;
	    private final LedgerService ledger;

	    public PaymentProcessingService(PaymentsRepository repository,PaymentGatewayClient gateway,LedgerService ledger) {
	        this.repository = repository;
	        this.gateway = gateway;
	        this.ledger = ledger;
	    }

	    @Transactional
	    public void process(PaymentCommand cmd) {

	        Payments payment = repository.findById(cmd.paymentId())
	            .orElseThrow(() -> new IllegalStateException(
	                "Payment not found: " + cmd.paymentId()
	            ));

	        // 🔐 Idempotency guard
	        if (payment.getStatus() == PaymentStatus.COMPLETED) {
	            return;
	        }

	        if (payment.getAmount() <= 0) {
	            payment.setStatus(PaymentStatus.FAILED);
	            repository.save(payment);
	            throw new NonRetryablePaymentException("Invalid amount");
	        }

	        try {
	            payment.setStatus(PaymentStatus.PROCESSING);
	            repository.save(payment);

	            gateway.charge(cmd);
	            ledger.record(cmd);

	            payment.setStatus(PaymentStatus.COMPLETED);
	            repository.save(payment);

	        } catch (Exception ex) {
	            payment.setRetryCount(payment.getRetryCount() + 1);
	            payment.setStatus(PaymentStatus.FAILED);
	            repository.save(payment);
	            throw new RetryablePaymentException("Temporary failure", ex);
	        }
	    }
	}