# async-task-processing

A hands-on learning repo for async task processing patterns in Spring Boot 4 / Spring
Framework 7 (Java 21).

## Projects

### [`order/`](order/) — Order Fulfillment Service

A simulated order-fulfillment API (payment → email/inventory/shipping) used to work
through async processing end to end:

- `@Async` + `CompletableFuture` for parallelizing independent steps
- A synchronous "gate" step (payment) that must complete before the rest run
- A fire-and-forget REST API (`202 Accepted` + status polling) instead of a blocking response
- A real thread-pool deadlock (parent task blocking on child tasks from the same bounded
  pool) hit under load, and how splitting executors fixes it
- Migrating from a bounded `ThreadPoolTaskExecutor` to virtual threads, and why that
  removes the deadlock class entirely
- Spring Framework 7's native API versioning (no hardcoded `/api/v1` in any controller)

See [`order/README.md`](order/README.md) for how to run it, the API, and the full
processing flow.

## Requirements

- Java 21+
- Maven (each project includes the `mvnw` wrapper, no local Maven install needed)
