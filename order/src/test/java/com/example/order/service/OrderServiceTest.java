package com.example.order.service;

import com.example.order.dto.OrderRequest;
import com.example.order.model.OrderRecord;
import com.example.order.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderStore orderStore;
    @Mock
    private OrderProcessor orderProcessor;

    @InjectMocks
    private OrderService orderService;

    @Test
    void submitOrder_savesPendingRecordAndDelegatesToProcessorWithoutWaiting() {
        OrderRequest request = new OrderRequest("customer-1", "widget", 2);

        String orderId = orderService.submitOrder(request);

        assertThat(orderId).isNotBlank();

        ArgumentCaptor<OrderRecord> recordCaptor = ArgumentCaptor.forClass(OrderRecord.class);
        verify(orderStore).save(recordCaptor.capture());
        assertThat(recordCaptor.getValue().orderId()).isEqualTo(orderId);
        assertThat(recordCaptor.getValue().status()).isEqualTo(OrderStatus.PENDING);

        verify(orderProcessor).process(eq(recordCaptor.getValue()), eq(request));
    }

    @Test
    void getStatus_delegatesToStore() {
        OrderRecord record = OrderRecord.pending("order-1");
        when(orderStore.find("order-1")).thenReturn(java.util.Optional.of(record));

        var result = orderService.getStatus("order-1");

        assertThat(result).contains(record);
    }
}
