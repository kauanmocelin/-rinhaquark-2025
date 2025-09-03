package dev.kauanmocelin;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
@Startup
public class PaymentWorker {

    @Inject
    RedisQueueConsumer consumer;

    @Inject
    ProcessPaymentUseCase processPaymentUseCase;

    @ConfigProperty(name = "redis.queue.payments.requests")
    private String paymentsQueue;

    @ConfigProperty(name = "payments.worker.size")
    long workerSize;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @PostConstruct
    void onStart() {
        Log.info("number of worker size: " + workerSize);
        for (int i = 0; i < workerSize; i++) {
            executor.submit(this::dispatchLoop);
        }
    }

    private void dispatchLoop() {
        while (true) {
            try {
                ProcessPaymentRequest request = consumer.take(paymentsQueue);
                if (request != null) {
                    processPaymentUseCase.execute(request);
                }
            } catch (Exception e) {
                Log.error("Error processing payment request", e);
            }
        }
    }
}
