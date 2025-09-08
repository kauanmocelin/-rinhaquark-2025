package dev.kauanmocelin.consumer.payment.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kauanmocelin.consumer.payment.ProcessPaymentRequest;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.list.KeyValue;
import io.quarkus.redis.datasource.list.ListCommands;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;

@ApplicationScoped
public class RedisQueueConsumer {

    private final ListCommands<String, String> listCommands;
    private final ObjectMapper mapper;

    public RedisQueueConsumer(RedisDataSource dataSource, ObjectMapper mapper) {
        this.listCommands = dataSource.list(String.class);
        this.mapper = mapper;
    }

    public ProcessPaymentRequest take(String queue) {
        KeyValue<String, String> entry = listCommands.blpop(Duration.ofSeconds(1), queue);
        if (entry != null && entry.value() != null) {
            try {
                return mapper.readValue(entry.value(), ProcessPaymentRequest.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
