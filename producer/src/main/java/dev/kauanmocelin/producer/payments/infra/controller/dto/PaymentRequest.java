package dev.kauanmocelin.producer.payments.infra.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
    UUID correlationId,
    BigDecimal amount) {
}
