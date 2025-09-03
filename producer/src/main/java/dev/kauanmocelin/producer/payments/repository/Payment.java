package dev.kauanmocelin.producer.payments.repository;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Payment {

    @JsonCreator
    public Payment(@JsonProperty("correlationId") UUID correlationId,
                   @JsonProperty("amount") BigDecimal amount,
                   @JsonProperty("requestedAt") Instant requestedAt,
                   @JsonProperty("paymentProcessorType") PaymentProcessorType paymentProcessorType) {
        this.correlationId = correlationId;
        this.amount = amount;
        this.requestedAt = requestedAt;
        this.paymentProcessorType = paymentProcessorType;
    }

    public Payment() {
    }

    public UUID correlationId;
    public BigDecimal amount;
    public Instant requestedAt;
    public PaymentProcessorType paymentProcessorType;

    public void setPaymentProcessorType(PaymentProcessorType paymentProcessorType) {
        this.paymentProcessorType = paymentProcessorType;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public PaymentProcessorType getPaymentProcessorType() {
        return paymentProcessorType;
    }
}
