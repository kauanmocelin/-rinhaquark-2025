package dev.kauanmocelin.producer.payments.application;

import dev.kauanmocelin.producer.payments.infra.controller.ProcessPaymentRequest;

public interface QueuePublisher {
    void publish(final String queue, final ProcessPaymentRequest message);
}
