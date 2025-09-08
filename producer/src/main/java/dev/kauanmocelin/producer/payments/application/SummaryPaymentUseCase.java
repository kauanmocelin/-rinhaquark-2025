package dev.kauanmocelin.producer.payments.application;

import dev.kauanmocelin.producer.payments.infra.controller.dto.PaymentSummary;
import dev.kauanmocelin.producer.payments.infra.controller.dto.PaymentSummaryResponse;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class SummaryPaymentUseCase {

    private final PaymentRepository paymentRepository;

    public SummaryPaymentUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentSummaryResponse execute(final Instant from, final Instant to) {
        List<PaymentSummary> summaryPayments = new ArrayList<>();
        if (Objects.nonNull(from) && Objects.nonNull(to)) {
            summaryPayments = paymentRepository.getPayments(from, to);
        }
        Map<String, PaymentSummary> map = summaryPayments.stream()
            .collect(Collectors.toMap(
                ps -> ps.type().toLowerCase(),
                ps -> new PaymentSummary(ps.type().toLowerCase(), ps.totalRequests(), ps.totalAmount())
            ));
        PaymentSummary defaultSummary = map.getOrDefault("default", new PaymentSummary("default", 0, BigDecimal.ZERO));
        PaymentSummary fallbackSummary = map.getOrDefault("fallback", new PaymentSummary("fallback", 0, BigDecimal.ZERO));
        return new PaymentSummaryResponse(defaultSummary, fallbackSummary);
    }
}
