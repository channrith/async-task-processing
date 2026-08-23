package com.example.order.dto;

import com.example.order.model.OrderRecord;
import com.example.order.model.OrderStatus;

public record OrderStatusResponse(
        String orderId,
        OrderStatus status,
        String transactionId,
        String errorMessage,
        Long elapsedMillis) {

    public static OrderStatusResponse from(OrderRecord record) {
        return new OrderStatusResponse(
                record.orderId(), record.status(), record.transactionId(), record.errorMessage(), record.elapsedMillis());
    }
}
