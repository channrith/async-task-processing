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

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderProcessorTest {

    @Mock
    private PaymentService paymentService;
    @Mock
    private EmailService emailService;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private ShippingService shippingService;
    @Mock
    private OrderStore orderStore;

    @InjectMocks
    private OrderProcessor orderProcessor;

    @Test
    void process_onPaymentSuccess_runsRemainingStepsAndSavesCompleted() {
        OrderRequest request = new OrderRequest("customer-1", "widget", 2);
        OrderRecord pending = OrderRecord.pending("order-1");
        when(paymentService.chargePayment(request)).thenReturn("txn-123");
        when(emailService.sendConfirmationEmail(request)).thenReturn(CompletableFuture.completedFuture(null));
        when(inventoryService.updateInventory(request)).thenReturn(CompletableFuture.completedFuture(null));
        when(shippingService.notifyShippingPartner(request)).thenReturn(CompletableFuture.completedFuture(null));

        orderProcessor.process(pending, request);

        ArgumentCaptor<OrderRecord> savedRecord = ArgumentCaptor.forClass(OrderRecord.class);
        verify(orderStore).save(savedRecord.capture());
        assertThat(savedRecord.getValue().status()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(savedRecord.getValue().transactionId()).isEqualTo("txn-123");

        verify(emailService).sendConfirmationEmail(request);
        verify(inventoryService).updateInventory(request);
        verify(shippingService).notifyShippingPartner(request);
    }

    @Test
    void process_onPaymentFailure_skipsRemainingStepsAndSavesFailed() {
        OrderRequest request = new OrderRequest("customer-1", "widget", 2);
        OrderRecord pending = OrderRecord.pending("order-1");
        when(paymentService.chargePayment(request)).thenThrow(new RuntimeException("card declined"));

        orderProcessor.process(pending, request);

        ArgumentCaptor<OrderRecord> savedRecord = ArgumentCaptor.forClass(OrderRecord.class);
        verify(orderStore).save(savedRecord.capture());
        assertThat(savedRecord.getValue().status()).isEqualTo(OrderStatus.FAILED);
        assertThat(savedRecord.getValue().errorMessage()).isEqualTo("card declined");

        // Payment gates the rest — none of these should ever run if it fails.
        verifyNoInteractions(emailService, inventoryService, shippingService);
    }
}
