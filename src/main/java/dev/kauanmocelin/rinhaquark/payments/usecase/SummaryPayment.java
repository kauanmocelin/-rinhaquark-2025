package dev.kauanmocelin.rinhaquark.payments.usecase;

import dev.kauanmocelin.rinhaquark.payments.client.SyncPaymentsClient;
import dev.kauanmocelin.rinhaquark.payments.controller.dto.PaymentSummary;
import dev.kauanmocelin.rinhaquark.payments.controller.dto.PaymentSummaryResponse;
import dev.kauanmocelin.rinhaquark.payments.infra.PaymentBatchService;
import dev.kauanmocelin.rinhaquark.payments.repository.PaymentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class SummaryPayment {

    private final PaymentRepository paymentRepository;
    private final PaymentBatchService paymentBatchService;
    @RestClient
    SyncPaymentsClient syncPaymentsClient;

    public SummaryPayment(PaymentRepository paymentRepository, PaymentBatchService paymentBatchService) {
        this.paymentRepository = paymentRepository;
        this.paymentBatchService = paymentBatchService;
    }

    public PaymentSummaryResponse execute(final Instant from, final Instant to) {
        syncPaymentsClient.syncPaymentsDatabase();
        paymentBatchService.flushBatch();

        List<PaymentSummary> summaryPayments = new ArrayList<>();
        if (Objects.nonNull(from) && Objects.nonNull(to)) {
            summaryPayments = paymentRepository.summaryPayments(from, to);
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

    public void sync() {
        paymentBatchService.flushBatch();
    }
}
