package com.spring.batch.kafka.ledger;

import org.springframework.stereotype.Service;

import com.spring.batch.kafka.model.PaymentCommand;

@Service
public class LedgerService {
    public void record(PaymentCommand payment) {
        System.out.println("Ledger updated for " + payment.paymentId());
    }
}
