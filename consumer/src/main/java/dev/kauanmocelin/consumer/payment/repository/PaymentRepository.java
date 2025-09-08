package dev.kauanmocelin.consumer.payment.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.sortedset.SortedSetCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class PaymentRepository {

    private final SortedSetCommands<String, String> zsetCommand;
    private final String paymentKey;
    private final ObjectMapper objectMapper;

    @Inject
    public PaymentRepository(RedisDataSource ds,
                             @ConfigProperty(name = "redis.payment.records") String paymentKey, ObjectMapper objectMapper) {
        this.zsetCommand = ds.sortedSet(String.class, String.class);
        this.paymentKey = paymentKey;
        this.objectMapper = objectMapper;
    }

    public void savePayment(PaymentRedisRecord paymentRedisRecord) {
        final double score = paymentRedisRecord.requestedAt().toEpochMilli();
        try {
            final String paymentRecordJson = objectMapper.writeValueAsString(paymentRedisRecord);
            zsetCommand.zadd(paymentKey, score, paymentRecordJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
