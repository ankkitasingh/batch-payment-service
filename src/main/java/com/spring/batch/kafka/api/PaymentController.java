package com.spring.batch.kafka.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.batch.kafka.entity.Payments;
import com.spring.batch.kafka.enu.PaymentStatus;
import com.spring.batch.kafka.model.PaymentCommand;
import com.spring.batch.kafka.producer.PaymentProducer;
import com.spring.batch.kafka.repository.PaymentsRepository;

@RestController
@RequestMapping("/payments")
public class PaymentController {

	private final PaymentsRepository repository;
    private final PaymentProducer producer;

    public PaymentController(PaymentsRepository repository,PaymentProducer producer) {
    	this.repository = repository;
        this.producer = producer;
    }

   
    @PostMapping
    public ResponseEntity<String> pay(@RequestBody PaymentCommand command) {
    
    	 Payments payment = new Payments();
         payment.setPaymentId(command.paymentId());
         payment.setAccountId(command.accountId());
         payment.setAmount(command.amount());
         payment.setCurrency(command.currency());
         payment.setStatus(PaymentStatus.RECEIVED);
         payment.setRetryCount(0);

         repository.save(payment);

         producer.send(command);

         return ResponseEntity.accepted().body("Payment accepted");
    }
}