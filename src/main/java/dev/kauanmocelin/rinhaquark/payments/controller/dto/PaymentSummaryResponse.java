package dev.kauanmocelin.rinhaquark.payments.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record PaymentSummaryResponse(
    @JsonProperty("default")
    @NotNull
    PaymentSummary defaultValue,

    @NotNull
    PaymentSummary fallback
) {
}
