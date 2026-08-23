package com.example.order.service;

import com.example.order.dto.OrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class InventoryService {

    // Runs only after payment succeeds (see OrderService), in parallel with email and shipping.
    @Async("taskExecutor")
    public CompletableFuture<Void> updateInventory(OrderRequest request) {
        log.info("Updating inventory for item {} (qty {})", request.item(), request.quantity());
        sleep(1000);
        log.info("Inventory updated for item {}", request.item());
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
