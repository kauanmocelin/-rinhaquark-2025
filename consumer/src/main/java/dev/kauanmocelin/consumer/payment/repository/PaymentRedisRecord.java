package dev.kauanmocelin.consumer.payment.repository;


import io.quarkus.runtime.annotations.RegisterForReflection;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RegisterForReflection
public record PaymentRedisRecord(
    UUID correlationId,
    BigDecimal amount,
    Instant requestedAt,
    PaymentProcessorType
    paymentProcessorType
) {
}
