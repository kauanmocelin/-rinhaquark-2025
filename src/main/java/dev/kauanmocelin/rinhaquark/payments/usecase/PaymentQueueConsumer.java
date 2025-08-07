package dev.kauanmocelin.rinhaquark.payments.usecase;

import dev.kauanmocelin.rinhaquark.payments.client.ProcessPaymentRequest;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Startup
@ApplicationScoped
public class PaymentQueueConsumer {

    @Inject
    InMemoryQueue queue;

    @Inject
    ProcessPayment processPayment;

    private ExecutorService executor;
    private final int threadCount = 4;

    private static final Logger LOG = Logger.getLogger(PaymentQueueConsumer.class);

    @PostConstruct
    void onStart() {
//        executor = Executors.newFixedThreadPool(threadCount);
        executor = Executors.newVirtualThreadPerTaskExecutor();

        for (int i = 0; i < threadCount; i++) {
            int threadIndex = i; // para nomear a thread
            executor.submit(() -> {
                Thread.currentThread().setName("queue-consumer-" + threadIndex);
                consumeLoop();
            });
        }
    }


    private void consumeLoop() {
        AtomicInteger consumed = new AtomicInteger(0);
        while (true) {
            ProcessPaymentRequest request = queue.take();
            if (request != null) {
                processPayment.execute(request);
            }
        }
    }

    @PreDestroy
    void onStop() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
