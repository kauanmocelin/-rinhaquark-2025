package dev.kauanmocelin.producer.payments.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kauanmocelin.producer.payments.infra.controller.dto.PaymentSummary;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.sortedset.ScoreRange;
import io.quarkus.redis.datasource.sortedset.SortedSetCommands;
import io.quarkus.redis.datasource.sortedset.ZRangeArgs;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class PaymentRepository {

    private final SortedSetCommands<String, String> zset;
    @Inject
    ObjectMapper objectMapper;

    @Inject
    public PaymentRepository(RedisDataSource ds) {
        this.zset = ds.sortedSet(String.class, String.class);
    }

    private Payment parsePayment(String json) {
        try {
            return objectMapper.readValue(json, Payment.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter JSON para Payment: " + json, e);
        }
    }

    public List<PaymentSummary> getPayments(Instant from, Instant to) {
        String key = "payments:zset";

        ScoreRange<Double> range = ScoreRange.from(Double.valueOf(from.toEpochMilli()), Double.valueOf(to.toEpochMilli()));

        List<String> paymentsJson = zset.zrangebyscore(key, range, new ZRangeArgs());

        return paymentsJson.stream()
            .map(this::parsePayment)
            .collect(Collectors.groupingBy(
                p -> p.getPaymentProcessorType(),
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> new PaymentSummary(
                        list.getFirst().getPaymentProcessorType().name(),
                        list.size(),
                        list.stream()
                            .map(Payment::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                    )
                )
            ))
            .values()
            .stream()
            .toList();
    }
}