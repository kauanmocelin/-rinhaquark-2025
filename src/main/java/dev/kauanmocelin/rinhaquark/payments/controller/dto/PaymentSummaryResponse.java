package dev.kauanmocelin.rinhaquark.payments.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentSummaryResponse(
    @JsonProperty("default")
    PaymentSummary defaultValue,

    PaymentSummary fallback
) {
}
