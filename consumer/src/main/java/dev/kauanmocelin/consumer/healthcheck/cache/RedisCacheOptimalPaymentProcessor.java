package dev.kauanmocelin.consumer.healthcheck.cache;

import dev.kauanmocelin.consumer.payment.repository.PaymentProcessorType;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class RedisCacheOptimalPaymentProcessor {

    private final ValueCommands<String, PaymentProcessorType> valueCommands;
    private final String cacheKeyOptimalProcessor;

    public RedisCacheOptimalPaymentProcessor(RedisDataSource dataSource,
                                             @ConfigProperty(name = "redis.payment.processor.optimal") String cacheKeyOptimalProcessor) {
        this.valueCommands = dataSource.value(String.class, PaymentProcessorType.class);
        this.cacheKeyOptimalProcessor = cacheKeyOptimalProcessor;
    }

    public void setOptimalPaymentProcessor(final PaymentProcessorType paymentProcessorType) {
        valueCommands.set(cacheKeyOptimalProcessor, paymentProcessorType);
    }
}