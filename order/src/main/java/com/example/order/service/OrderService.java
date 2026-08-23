package com.example.order.service;

import com.example.order.dto.OrderRequest;
import com.example.order.model.OrderRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderStore orderStore;
    private final OrderProcessor orderProcessor;

    // Returns as soon as the order is recorded as PENDING — actual processing
    // (payment, email, inventory, shipping) happens on a background thread via
    // OrderProcessor and is not awaited here.
    public String submitOrder(OrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        OrderRecord pendingRecord = OrderRecord.pending(orderId);
        orderStore.save(pendingRecord);
        orderProcessor.process(pendingRecord, request);
        return orderId;
    }

    public Optional<OrderRecord> getStatus(String orderId) {
        return orderStore.find(orderId);
    }
}
