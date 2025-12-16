package com.spring.batch.kafka.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.batch.kafka.entity.Payments;

public interface PaymentsRepository extends JpaRepository<Payments, String> {
	
}
