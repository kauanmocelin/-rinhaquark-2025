package dev.kauanmocelin.rinhaquark.payments.repository;


import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@MongoEntity(collection = "payments")
public class Payment {

    public Payment(UUID correlationId, BigDecimal amount, Instant requestedAt, PaymentProcessorType paymentProcessorType) {
        this.correlationId = correlationId;
        this.amount = amount;
        this.requestedAt = requestedAt;
        this.paymentProcessorType = paymentProcessorType;
    }

    public Payment() {
    }

    @NotNull
    public UUID correlationId;
    @NotNull
    @Positive
    public BigDecimal amount;
    @NotNull
    public Instant requestedAt;
    public PaymentProcessorType paymentProcessorType;
}
