package com.example.order.service;

import com.example.order.dto.OrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class PaymentService {

    // Simulates a real gateway occasionally declining a charge, so OrderProcessor's
    // failure path (skip email/inventory/shipping, save FAILED) has something to catch
    // in the running app, not just in mocked tests.
    private static final double DECLINE_RATE = 0.1;

    // Deliberately synchronous: the rest of the order (email/inventory/shipping)
    // must not start until payment is confirmed, so there is nothing to gain by
    // running this on a separate thread. The caller (OrderProcessor.process, itself
    // already running on a background thread) would just call .join() on it
    // immediately anyway — this doesn't block the HTTP response, only the
    // background order-processing thread that's already handling this order.
    public String chargePayment(OrderRequest request) {
        log.info("Charging payment for customer {}", request.customerId());
        sleep(2000);

        if (ThreadLocalRandom.current().nextDouble() < DECLINE_RATE) {
            log.warn("Payment declined for customer {}", request.customerId());
            throw new PaymentDeclinedException("Payment declined for customer " + request.customerId());
        }

        String transactionId = UUID.randomUUID().toString();
        log.info("Payment charged, transactionId={}", transactionId);
        return transactionId;
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
