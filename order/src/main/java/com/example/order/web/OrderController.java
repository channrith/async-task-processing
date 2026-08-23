package com.example.order.web;

import com.example.order.dto.OrderAcceptedResponse;
import com.example.order.dto.OrderRequest;
import com.example.order.dto.OrderStatusResponse;
import com.example.order.model.OrderStatus;
import com.example.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.base-path}/{version}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping(version = "1")
    public ResponseEntity<OrderAcceptedResponse> placeOrder(@RequestBody OrderRequest request) {
        String orderId = orderService.submitOrder(request);
        return ResponseEntity.accepted().body(new OrderAcceptedResponse(orderId, OrderStatus.PENDING));
    }

    @GetMapping(value = "/{orderId}", version = "1")
    public ResponseEntity<OrderStatusResponse> getOrderStatus(@PathVariable String orderId) {
        return orderService.getStatus(orderId)
                .map(record -> ResponseEntity.ok(OrderStatusResponse.from(record)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
