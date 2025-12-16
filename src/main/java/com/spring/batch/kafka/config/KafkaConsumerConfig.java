package com.spring.batch.kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.util.backoff.FixedBackOff;

import com.spring.batch.kafka.exception.NonRetryablePaymentException;
import com.spring.batch.kafka.model.PaymentCommand;
import org.springframework.kafka.listener.ContainerProperties;


@Configuration
public class KafkaConsumerConfig {
	
	@Bean
	ConcurrentKafkaListenerContainerFactory<String, PaymentCommand> batchFactory(
	        ConsumerFactory<String, PaymentCommand> cf,
	        KafkaTemplate<String, Object> template) {

	    ConcurrentKafkaListenerContainerFactory<String, PaymentCommand> factory =
	            new ConcurrentKafkaListenerContainerFactory<>();

	    factory.setConsumerFactory(cf);
	    factory.setBatchListener(true);

	    // ✅ THIS IS THE MISSING LINE
	    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

	    DefaultErrorHandler errorHandler =
	            new DefaultErrorHandler(
	                    new DeadLetterPublishingRecoverer(template),
	                    new FixedBackOff(2000, 3)
	            );

	    errorHandler.addNotRetryableExceptions(NonRetryablePaymentException.class);

	    factory.setCommonErrorHandler(errorHandler);

	    return factory;
	}


}
