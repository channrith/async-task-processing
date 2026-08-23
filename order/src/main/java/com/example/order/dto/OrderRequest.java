package com.example.order.dto;

public record OrderRequest(String customerId, String item, int quantity) {
}
