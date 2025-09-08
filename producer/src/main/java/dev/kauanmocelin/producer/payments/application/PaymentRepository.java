package dev.kauanmocelin.producer.payments.application;

import dev.kauanmocelin.producer.payments.infra.controller.dto.PaymentSummary;

import java.time.Instant;
import java.util.List;

public interface PaymentRepository {

    List<PaymentSummary> getPayments(Instant from, Instant to);
}
