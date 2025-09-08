package dev.kauanmocelin.consumer.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProcessPaymentRequest(
    UUID correlationId,
    BigDecimal amount,
    Instant requestedAt
) {
}