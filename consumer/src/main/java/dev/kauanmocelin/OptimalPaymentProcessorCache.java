package dev.kauanmocelin;

import dev.kauanmocelin.consumer.PaymentProcessorType;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OptimalPaymentProcessorCache {

    private final ValueCommands<String, PaymentProcessorType> valueCommands;

    public OptimalPaymentProcessorCache(RedisDataSource dataSource) {
        this.valueCommands = dataSource.value(String.class, PaymentProcessorType.class);
    }

    public void setOptimalPaymentProcessor(final PaymentProcessorType paymentProcessorType) {
        valueCommands.setex("payment:processor:optimal", 5, paymentProcessorType);
    }
}
