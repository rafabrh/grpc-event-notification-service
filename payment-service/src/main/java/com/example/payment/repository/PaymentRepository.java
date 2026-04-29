package com.example.payment.repository;

import com.example.payment.domain.Payment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repository in-memory para exemplo.
 * Em produção real, usar Spring Data JPA/MongoDB.
 */
@Repository
public class PaymentRepository {
    
    private final Map<String, Payment> database = new ConcurrentHashMap<>();
    
    public Payment save(Payment payment) {
        database.put(payment.getPaymentId(), payment);
        return payment;
    }
    
    public Optional<Payment> findById(String paymentId) {
        return Optional.ofNullable(database.get(paymentId));
    }
    
    public List<Payment> findByCustomerId(String customerId) {
        return database.values().stream()
                .filter(p -> customerId.equals(p.getCustomerId()))
                .collect(Collectors.toList());
    }

    public Map<String, Payment> findAll() {
        return Map.copyOf(database);
    }
}
