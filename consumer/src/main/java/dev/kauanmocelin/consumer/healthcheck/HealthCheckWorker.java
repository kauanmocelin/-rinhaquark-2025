package dev.kauanmocelin.consumer.healthcheck;

import dev.kauanmocelin.consumer.healthcheck.cache.RedisCacheOptimalPaymentProcessor;
import dev.kauanmocelin.consumer.healthcheck.client.DefaultPaymentProcessorHealthClient;
import dev.kauanmocelin.consumer.healthcheck.client.FallbackPaymentProcessorHealthClient;
import dev.kauanmocelin.consumer.payment.repository.PaymentProcessorType;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class HealthCheckWorker {

    private static final int MAX_DEFAULT_RESPONSE_TIME_MS = 120;
    private static final int DEFAULT_THRESHOLD_MULTIPLIER = 3;
    private final DefaultPaymentProcessorHealthClient defaultHealthClient;
    private final FallbackPaymentProcessorHealthClient fallbackHealthClient;
    private final RedisCacheOptimalPaymentProcessor redisCacheOptimalPaymentProcessor;

    public HealthCheckWorker(@RestClient DefaultPaymentProcessorHealthClient defaultHealthClient,
                             @RestClient FallbackPaymentProcessorHealthClient fallbackHealthClient,
                             RedisCacheOptimalPaymentProcessor redisCacheOptimalPaymentProcessor) {
        this.defaultHealthClient = defaultHealthClient;
        this.fallbackHealthClient = fallbackHealthClient;
        this.redisCacheOptimalPaymentProcessor = redisCacheOptimalPaymentProcessor;
    }

    @Scheduled(every = "5s")
    void checkHealth() {
        final HealthCheckResponse defaultCheckResponse = defaultHealthClient.checkHealth();
        final HealthCheckResponse fallbackCheckResponse = fallbackHealthClient.checkHealth();
        if (defaultCheckResponse.failing()) {
            redisCacheOptimalPaymentProcessor.setOptimalPaymentProcessor(PaymentProcessorType.FALLBACK);
            return;
        }
        if (defaultCheckResponse.minResponseTime() < MAX_DEFAULT_RESPONSE_TIME_MS) {
            redisCacheOptimalPaymentProcessor.setOptimalPaymentProcessor(PaymentProcessorType.DEFAULT);
            return;
        }
        if (!fallbackCheckResponse.failing() && fallbackCheckResponse.minResponseTime() < defaultCheckResponse.minResponseTime() * DEFAULT_THRESHOLD_MULTIPLIER) {
            redisCacheOptimalPaymentProcessor.setOptimalPaymentProcessor(PaymentProcessorType.FALLBACK);
            return;
        }
        redisCacheOptimalPaymentProcessor.setOptimalPaymentProcessor(PaymentProcessorType.DEFAULT);
    }
}
