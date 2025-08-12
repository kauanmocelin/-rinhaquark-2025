package dev.kauanmocelin.rinhaquark.payments.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
    UUID correlationId,
    BigDecimal amount) {
}
