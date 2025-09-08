package dev.kauanmocelin.consumer.payment.queue;

import dev.kauanmocelin.consumer.payment.ProcessPaymentRequest;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.list.ListCommands;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RedisQueuePublisher {

    private final ListCommands<String, ProcessPaymentRequest> listCommands;

    public RedisQueuePublisher(RedisDataSource dataSource) {
        listCommands = dataSource.list(ProcessPaymentRequest.class);
    }

    public void publish(String queue, ProcessPaymentRequest message) {
        listCommands.rpush(queue, message);
    }
}
