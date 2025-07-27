package dev.kauanmocelin.rinhaquark.payments.client;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProcessPaymentRequest(
    @NotNull
    UUID correlationId,
    @NotNull
    @Positive
    BigDecimal amount,
    @NotNull
    Instant requestedAt
) {
}