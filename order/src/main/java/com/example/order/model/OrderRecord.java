package com.example.order.model;

public record OrderRecord(
        String orderId,
        OrderStatus status,
        String transactionId,
        String errorMessage,
        Long elapsedMillis) {

    public static OrderRecord pending(String orderId) {
        return new OrderRecord(orderId, OrderStatus.PENDING, null, null, null);
    }

    public OrderRecord completed(String transactionId, long elapsedMillis) {
        return new OrderRecord(orderId, OrderStatus.COMPLETED, transactionId, null, elapsedMillis);
    }

    public OrderRecord failed(String errorMessage, long elapsedMillis) {
        return new OrderRecord(orderId, OrderStatus.FAILED, null, errorMessage, elapsedMillis);
    }
}
