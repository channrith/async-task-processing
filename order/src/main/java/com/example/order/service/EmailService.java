package com.example.order.service;

import com.example.order.dto.OrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class EmailService {

    // @Async only takes effect when called through Spring's proxy, i.e. from another
    // bean (OrderService) — calling this method from within EmailService itself would
    // silently run synchronously.
    @Async("taskExecutor")
    public CompletableFuture<Void> sendConfirmationEmail(OrderRequest request) {
        log.info("Sending confirmation email for customer {}", request.customerId());
        sleep(1500);
        log.info("Confirmation email sent to customer {}", request.customerId());
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
