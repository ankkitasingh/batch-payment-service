package com.spring.batch.kafka.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger log =
            LoggerFactory.getLogger(PaymentProcessingService.class);
	
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

	    	log.info("Processing paymentId={}, amount={}",
	                cmd.paymentId(), cmd.amount());
	    	
	    	Payments payment = repository.findById(cmd.paymentId())
	                .orElseThrow(() -> {
	                    log.error(" Payment not found: {}", cmd.paymentId());
	                    return new IllegalStateException(
	                        "Payment not found: " + cmd.paymentId()
	                    );
	        });

	        // 🔐 Idempotency guard
	    	 if (payment.getStatus() == PaymentStatus.COMPLETED) {
	             log.info("️ Payment {} already COMPLETED, skipping", cmd.paymentId());
	             return;
	         }

	    	 if (payment.getAmount() <= 0) {
	             log.warn(" Invalid amount for paymentId={}", cmd.paymentId());
	             payment.setStatus(PaymentStatus.FAILED);
	             repository.save(payment);
	             throw new NonRetryablePaymentException("Invalid amount");
	         }

	        try {
	        	 log.debug("Marking payment {} as PROCESSING", cmd.paymentId());
	             payment.setStatus(PaymentStatus.PROCESSING);
	             repository.save(payment);

	             log.debug("Charging payment {}", cmd.paymentId());
	             gateway.charge(cmd);

	             log.debug("Recording ledger entry for {}", cmd.paymentId());
	             ledger.record(cmd);

	             payment.setStatus(PaymentStatus.COMPLETED);
	             repository.save(payment);

	             log.info("Payment {} COMPLETED successfully", cmd.paymentId());

	        } catch (Exception ex) {
	        	payment.setRetryCount(payment.getRetryCount() + 1);
	            payment.setStatus(PaymentStatus.FAILED);
	            repository.save(payment);
	            log.error("Payment {} FAILED (retryCount={})",
	                    cmd.paymentId(),
	                    payment.getRetryCount(),
	                    ex);
	            throw new RetryablePaymentException("Temporary failure", ex);
	        }
	    }
	}