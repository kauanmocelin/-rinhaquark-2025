package dev.kauanmocelin.consumer;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Payment {

    public Payment(UUID correlationId, BigDecimal amount, Instant requestedAt, PaymentProcessorType paymentProcessorType) {
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
}
