package com.spring.batch.kafka.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.spring.batch.kafka.enu.PaymentStatus;

@Repository
public class PaymentRepository {

    private final Map<String, PaymentStatus> store = new ConcurrentHashMap<>();

    public Optional<PaymentStatus> find(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public void save(String id, PaymentStatus status) {
        store.put(id, status);
    }
}

