package dev.kauanmocelin.rinhaquark.payments.usecase;

import dev.kauanmocelin.rinhaquark.payments.controller.dto.PaymentSummary;
import dev.kauanmocelin.rinhaquark.payments.controller.dto.PaymentSummaryResponse;
import dev.kauanmocelin.rinhaquark.payments.repository.PaymentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class SummaryPayment {

    private static final Logger LOG = Logger.getLogger(SummaryPayment.class.getName());

    private final PaymentRepository paymentRepository;

    public SummaryPayment(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentSummaryResponse execute(final Instant from, final Instant to) {
        LOG.info("⏱️ Executing summary from=" + from + " to=" + to);

        List<Document> summaryPayments = paymentRepository.summaryPayments(from, to);

        Map<String, PaymentSummary> map = summaryPayments.stream()
            .collect(Collectors.toMap(
                doc -> doc.getString("type").toLowerCase(),
                doc -> new PaymentSummary(
                    doc.getInteger("totalRequests", 0),
                    doc.get("totalAmount", Decimal128.class).bigDecimalValue()
                )
            ));

        PaymentSummary defaultSummary = map.getOrDefault("default", new PaymentSummary(0, BigDecimal.ZERO));
        PaymentSummary fallbackSummary = map.getOrDefault("fallback", new PaymentSummary(0, BigDecimal.ZERO));

        LOG.info("✅ Summary result -> default: " + defaultSummary + " | fallback: \n" + fallbackSummary);

        return new PaymentSummaryResponse(defaultSummary, fallbackSummary);
    }
}
