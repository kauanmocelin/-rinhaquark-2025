package dev.kauanmocelin.consumer;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.sortedset.SortedSetCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PaymentRepository {

    private final SortedSetCommands<String, String> zset;

    @Inject
    public PaymentRepository(RedisDataSource ds) {
        this.zset = ds.sortedSet(String.class, String.class);
    }

    public void savePayment(Payment payment) {
        String key = "payments:zset";
        double score = payment.requestedAt.toEpochMilli();

        String value = String.format(
            "{\"correlationId\":\"%s\",\"paymentProcessorType\":\"%s\",\"amount\":%s,\"requestedAt\":%d}",
            payment.correlationId,
            payment.paymentProcessorType.name(),
            payment.amount,
            payment.requestedAt.toEpochMilli()
        );

        zset.zadd(key, score, value);
    }
}
