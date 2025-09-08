package dev.kauanmocelin.consumer.payment;

import dev.kauanmocelin.consumer.payment.queue.RedisQueueConsumer;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
@Startup
public class PaymentWorker {

    private final RedisQueueConsumer consumer;
    private final ProcessPaymentUseCase processPaymentUseCase;
    private final String paymentsQueue;
    private final long workerSize;
    private final ExecutorService executor;

    public PaymentWorker(RedisQueueConsumer consumer,
                         ProcessPaymentUseCase processPaymentUseCase,
                         @ConfigProperty(name = "redis.queue.payments.requests") String paymentsQueue,
                         @ConfigProperty(name = "payments.worker.size") long workerSize) {
        this.consumer = consumer;
        this.processPaymentUseCase = processPaymentUseCase;
        this.paymentsQueue = paymentsQueue;
        this.workerSize = workerSize;
        executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @PostConstruct
    void onStart() {
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
