package dev.kauanmocelin.producer.payments.infra.controller.dto;

import java.math.BigDecimal;

public record PaymentSummary(
    String type,
    Integer totalRequests,
    BigDecimal totalAmount
) {
}
