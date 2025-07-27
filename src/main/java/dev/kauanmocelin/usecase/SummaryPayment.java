package dev.kauanmocelin.usecase;

import dev.kauanmocelin.payments.controller.dto.PaymentSummary;
import dev.kauanmocelin.payments.controller.dto.PaymentSummaryResponse;
import dev.kauanmocelin.repository.PaymentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;
import org.bson.types.Decimal128;

import java.math.BigDecimal;
import java.time.Instant;

@ApplicationScoped
public class SummaryPayment {

    private final PaymentRepository paymentRepository;

    public SummaryPayment(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentSummaryResponse execute(final Instant from, final Instant to) {

        Document summaryPayments = paymentRepository.summaryPayments(from, to);

        if (summaryPayments != null && !summaryPayments.isEmpty()) {
            return new PaymentSummaryResponse(
                new PaymentSummary(summaryPayments.getInteger("totalRequests"), summaryPayments.get("totalAmount", Decimal128.class).bigDecimalValue()),
                new PaymentSummary(summaryPayments.getInteger("totalRequests"), summaryPayments.get("totalAmount", Decimal128.class).bigDecimalValue())
            );
        }
        return new PaymentSummaryResponse(
            new PaymentSummary(0, BigDecimal.ZERO),
            new PaymentSummary(0, BigDecimal.ZERO)
        );
    }
}
