# Order Fulfillment Service

A Spring Boot 4 learning project for async task processing, built around a simulated
order-fulfillment flow: charge payment, then email confirmation, update inventory, and
notify the shipping partner.

## Stack

- Spring Boot 4.1.1 / Spring Framework 7 (Java 21, virtual threads)
- Spring Web MVC, Spring Boot Actuator, Lombok

## Running it

```bash
./mvnw spring-boot:run
```

The app starts on **port 8081**.

## API

Versioned via Spring Framework 7's native API versioning (path-segment based,
configured in `web/config/WebConfig.java` — no version literal hardcoded in any
controller mapping).

### Submit an order

```bash
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"c1","item":"widget","quantity":2}'
```

Returns immediately (fire-and-forget):

```json
{"orderId":"...", "status":"PENDING"}
```

### Check order status

```bash
curl http://localhost:8081/api/v1/orders/{orderId}
```

```json
{"orderId":"...","status":"COMPLETED","transactionId":"...","errorMessage":null,"elapsedMillis":12010}
```

`status` is one of `PENDING`, `COMPLETED`, `FAILED`.

## How an order is processed

1. **Payment gates everything else.** `PaymentService.chargePayment` runs synchronously
   and must succeed before anything else starts (~2s simulated gateway delay, ~10%
   random decline rate to exercise the failure path).
2. **On success**, email confirmation, inventory update, and shipping notification run
   **concurrently** via `@Async` + `CompletableFuture` — total time is the slowest of
   the three (currently shipping, at 10s), not the sum.
3. **On failure**, the order is saved as `FAILED` with an error message, and the three
   steps above never run.

Expected timing: **~2s to `FAILED`**, **~12s to `COMPLETED`** (2s payment + 10s shipping).

The whole pipeline runs on a background thread (`OrderProcessor`, a separate bean from
`OrderService` so `@Async` isn't defeated by self-invocation) — the HTTP response
returns as soon as the order is recorded as `PENDING`, never waiting on any of this.

## Concurrency model: virtual threads

Both executors (`config/AsyncConfig.java`) are backed by virtual threads
(`SimpleAsyncTaskExecutor.setVirtualThreads(true)`), not a bounded platform-thread pool.
This matters for a reason beyond throughput: an earlier version used
`ThreadPoolTaskExecutor` with a small bounded pool, and `OrderProcessor.process()`
(which blocks on `.join()` waiting for the leaf steps) shared a pool with those leaf
steps. Under load, every core thread ended up permanently blocked waiting on child
tasks that could never get a thread to run on — a real deadlock, reproduced with 25
concurrent orders. Splitting into two executors (`taskExecutor` for leaf steps,
`orderProcessingExecutor` for orchestration) fixed the immediate issue; switching both
to virtual threads removes the failure class entirely, since a blocked virtual thread
unmounts from its OS carrier instead of occupying a scarce pool slot. Verified with 60
concurrent orders and no deadlock.

## State storage

Order status is kept in an in-memory `ConcurrentHashMap` (`OrderStore`) — fine for this
project, but it means state is lost on restart and isn't shared across instances. A real
deployment would back this with a database.

## Tests

```bash
./mvnw test
```

- `OrderServiceTest` — the thin orchestrator delegates to `OrderProcessor` without
  waiting, and status lookups delegate to the store.
- `OrderProcessorTest` — the success path runs all four steps and saves `COMPLETED`;
  the failure path saves `FAILED` and asserts email/inventory/shipping are never
  invoked when payment fails.
