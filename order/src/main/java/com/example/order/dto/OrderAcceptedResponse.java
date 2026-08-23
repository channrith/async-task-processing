package com.example.order.dto;

import com.example.order.model.OrderStatus;

public record OrderAcceptedResponse(String orderId, OrderStatus status) {
}
