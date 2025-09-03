package dev.kauanmocelin;

import dev.kauanmocelin.consumer.HealthCheckResponse;
import dev.kauanmocelin.consumer.PaymentProcessorType;
import dev.kauanmocelin.consumer.client.DefaultPaymentProcessorHealthClient;
import dev.kauanmocelin.consumer.client.FallbackPaymentProcessorHealthClient;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class HealthCheckWorker {

    @RestClient
    DefaultPaymentProcessorHealthClient defaultHealthClient;

    @RestClient
    FallbackPaymentProcessorHealthClient fallbackHealthClient;

    @Inject
    OptimalPaymentProcessorCache optimalPaymentProcessorCache;

    @Scheduled(every = "5s")
    void checkHealth() {
        HealthCheckResponse defaultCheckResponse = defaultHealthClient.checkHealth();
        HealthCheckResponse fallbackCheckResponse = fallbackHealthClient.checkHealth();

        optimalPaymentProcessorCache.setOptimalPaymentProcessor(PaymentProcessorType.FALLBACK);

        Log.infof("default health check: %s", defaultCheckResponse);
        Log.infof("fallback health check: %s", fallbackCheckResponse);
    }
}
