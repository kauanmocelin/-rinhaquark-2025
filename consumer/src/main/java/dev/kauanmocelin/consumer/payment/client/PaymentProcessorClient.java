package dev.kauanmocelin.consumer.payment.client;

import dev.kauanmocelin.consumer.payment.ProcessPaymentRequest;

public interface PaymentProcessorClient {
    void processPayment(ProcessPaymentRequest request);
}
