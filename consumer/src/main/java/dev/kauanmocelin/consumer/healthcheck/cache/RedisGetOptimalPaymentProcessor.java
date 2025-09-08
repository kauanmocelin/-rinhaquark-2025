package dev.kauanmocelin.consumer.healthcheck.cache;

import dev.kauanmocelin.consumer.payment.repository.PaymentProcessorType;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Objects;

@ApplicationScoped
public class RedisGetOptimalPaymentProcessor {

    private final ValueCommands<String, PaymentProcessorType> valueCommands;
    private final String cacheKeyOptimalProcessor;

    public RedisGetOptimalPaymentProcessor(RedisDataSource dataSource,
                                           @ConfigProperty(name = "redis.payment.processor.optimal") String cacheKeyOptimalProcessor) {
        this.valueCommands = dataSource.value(String.class, PaymentProcessorType.class);
        this.cacheKeyOptimalProcessor = cacheKeyOptimalProcessor;
    }

    public PaymentProcessorType getOptimalPaymentProcessor() {
        final PaymentProcessorType paymentProcessorType = valueCommands.get(cacheKeyOptimalProcessor);
        return Objects.requireNonNullElse(
            paymentProcessorType,
            PaymentProcessorType.DEFAULT
        );
    }
}