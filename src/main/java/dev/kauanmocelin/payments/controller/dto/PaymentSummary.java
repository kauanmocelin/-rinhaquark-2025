package dev.kauanmocelin.payments.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentSummary(
    @NotNull
    @Positive
    Integer totalRequests,
    @NotNull
    @Positive
    BigDecimal totalAmount
) {
}
