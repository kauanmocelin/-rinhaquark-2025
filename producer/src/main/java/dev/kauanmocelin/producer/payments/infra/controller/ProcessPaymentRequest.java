package dev.kauanmocelin.producer.payments.infra.controller;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RegisterForReflection
public record ProcessPaymentRequest(
    UUID correlationId,
    BigDecimal amount,
    Instant requestedAt
) {
}