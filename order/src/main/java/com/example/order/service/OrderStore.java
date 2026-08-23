package com.example.order.service;

import com.example.order.model.OrderRecord;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// In-memory only: fine for learning/demo, but state is lost on restart and isn't
// shared across instances. A real deployment would back this with a database.
@Component
public class OrderStore {

    private final Map<String, OrderRecord> records = new ConcurrentHashMap<>();

    public void save(OrderRecord record) {
        records.put(record.orderId(), record);
    }

    public Optional<OrderRecord> find(String orderId) {
        return Optional.ofNullable(records.get(orderId));
    }
}
