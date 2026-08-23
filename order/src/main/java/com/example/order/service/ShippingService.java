package com.example.order.service;

import com.example.order.dto.OrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class ShippingService {

    // Runs only after payment succeeds (see OrderService), in parallel with email and inventory.
    @Async("taskExecutor")
    public CompletableFuture<Void> notifyShippingPartner(OrderRequest request) {
        log.info("Notifying shipping partner for customer {}", request.customerId());
        sleep(10000);
        log.info("Shipping partner notified for customer {}", request.customerId());
        return CompletableFuture.completedFuture(null);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
