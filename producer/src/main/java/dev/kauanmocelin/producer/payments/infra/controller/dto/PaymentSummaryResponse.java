package dev.kauanmocelin.producer.payments.infra.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentSummaryResponse(

    @JsonProperty("default")
    PaymentSummary defaultValue,

    @JsonProperty("fallback")
    PaymentSummary fallbackValue
) {
}
