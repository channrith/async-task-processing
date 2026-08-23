package com.example.order.service;

import com.example.order.dto.OrderRequest;
import com.example.order.model.OrderRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

// Separate bean from OrderService on purpose: @Async only takes effect through
// Spring's proxy, so the entry point that starts background work must live on a
// different bean than the one that calls it (self-invocation from within
// OrderService would silently run synchronously and block the HTTP response).
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessor {

    private final PaymentService paymentService;
    private final EmailService emailService;
    private final InventoryService inventoryService;
    private final ShippingService shippingService;
    private final OrderStore orderStore;

    // Runs the entire order lifecycle in the background, off the request thread,
    // so the controller can return 202 Accepted the moment the order is stored as PENDING.
    //
    // Uses orderProcessingExecutor, NOT taskExecutor: this method blocks on .join()
    // waiting for taskExecutor's work, so it must not compete with that pool for
    // threads — see AsyncConfig for why sharing a pool here causes a deadlock.
    @Async("orderProcessingExecutor")
    public void process(OrderRecord pendingRecord, OrderRequest request) {
        String orderId = pendingRecord.orderId();
        long start = System.currentTimeMillis();
        try {
            // Payment gates the rest in the background instead of gating the HTTP response.
            String transactionId = paymentService.chargePayment(request);

            CompletableFuture<Void> emailFuture = emailService.sendConfirmationEmail(request);
            CompletableFuture<Void> inventoryFuture = inventoryService.updateInventory(request);
            CompletableFuture<Void> shippingFuture = shippingService.notifyShippingPartner(request);
            CompletableFuture.allOf(emailFuture, inventoryFuture, shippingFuture).join();

            long elapsed = System.currentTimeMillis() - start;
            orderStore.save(pendingRecord.completed(transactionId, elapsed));
            log.info("Order {} completed in {} ms", orderId, elapsed);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            orderStore.save(pendingRecord.failed(e.getMessage(), elapsed));
            log.error("Order {} failed after {} ms", orderId, elapsed, e);
        }
    }
}
