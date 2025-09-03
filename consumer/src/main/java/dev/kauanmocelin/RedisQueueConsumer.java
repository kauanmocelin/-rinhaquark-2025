package dev.kauanmocelin;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.list.KeyValue;
import io.quarkus.redis.datasource.list.ListCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;

@ApplicationScoped
public class RedisQueueConsumer {

    private final ListCommands<String, String> listCommands;

    @Inject
    private ObjectMapper mapper;

    public RedisQueueConsumer(RedisDataSource dataSource) {
        this.listCommands = dataSource.list(String.class);
    }

    public ProcessPaymentRequest take(String queue) throws Exception {
        KeyValue<String, String> entry = listCommands.blpop(Duration.ofSeconds(1), queue);

        if (entry != null && entry.value() != null) {
            return mapper.readValue(entry.value(), ProcessPaymentRequest.class);
        }
        return null;
    }
}
