package dev.kauanmocelin.producer.payments.infra.queue;

import dev.kauanmocelin.producer.payments.application.QueuePublisher;
import dev.kauanmocelin.producer.payments.infra.controller.dto.ProcessPaymentRequest;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.list.ListCommands;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RedisQueuePublisher implements QueuePublisher {

    private final ListCommands<String, ProcessPaymentRequest> listCommands;

    public RedisQueuePublisher(RedisDataSource dataSource) {
        listCommands = dataSource.list(ProcessPaymentRequest.class);
    }

    @Override
    public void publish(String queue, ProcessPaymentRequest message) {
        listCommands.rpush(queue, message);
    }
}
