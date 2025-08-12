package dev.kauanmocelin.rinhaquark.payments.controller.dto;

import java.math.BigDecimal;

public record PaymentSummary(
    String type,
    Integer totalRequests,
    BigDecimal totalAmount
) {
}
