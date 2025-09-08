package dev.kauanmocelin.producer.payments.infra.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kauanmocelin.producer.payments.application.PaymentRepository;
import dev.kauanmocelin.producer.payments.infra.controller.dto.PaymentSummary;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.sortedset.ScoreRange;
import io.quarkus.redis.datasource.sortedset.SortedSetCommands;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class RedisPaymentRepository implements PaymentRepository {

    private final SortedSetCommands<String, String> zsetCommand;
    private final String paymentKey;
    private final ObjectMapper objectMapper;

    public RedisPaymentRepository(RedisDataSource ds,
                                  @ConfigProperty(name = "redis.payment.records") String paymentKey,
                                  ObjectMapper objectMapper) {
        this.zsetCommand = ds.sortedSet(String.class, String.class);
        this.paymentKey = paymentKey;
        this.objectMapper = objectMapper;
    }

    public List<PaymentSummary> getPayments(Instant from, Instant to) {
        ScoreRange<Double> range = ScoreRange.from((double) from.toEpochMilli(), (double) to.toEpochMilli());
        List<String> paymentsJson = zsetCommand.zrangebyscore(paymentKey, range);
        return paymentsJson.stream()
            .map(this::parsePayment)
            .collect(Collectors.groupingBy(
                PaymentRedisRecord::paymentProcessorType,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> new PaymentSummary(
                        list.getFirst().paymentProcessorType().name(),
                        list.size(),
                        list.stream()
                            .map(PaymentRedisRecord::amount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                    )
                )
            ))
            .values()
            .stream()
            .toList();
    }

    private PaymentRedisRecord parsePayment(String json) {
        try {
            return objectMapper.readValue(json, PaymentRedisRecord.class);
        } catch (Exception e) {
            throw new RuntimeException("error converting JSON to Payment: " + json, e);
        }
    }
}