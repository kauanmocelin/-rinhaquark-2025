package dev.kauanmocelin.rinhaquark.payments.infra;

import dev.kauanmocelin.rinhaquark.payments.controller.ProcessPaymentRequest;
import dev.kauanmocelin.rinhaquark.payments.usecase.ProcessPayment;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Startup
@ApplicationScoped
public class PaymentQueueConsumer {

    @Inject
    PaymentQueue queue;

    @Inject
    ProcessPayment processPayment;

    @ConfigProperty(name = "payments.threads.number")
    long threadsNumber;

    private volatile boolean running = true;
    private ExecutorService executor;

    @PostConstruct
    void onStart() {
        executor = Executors.newVirtualThreadPerTaskExecutor();
        for (int i = 0; i < threadsNumber; i++) {
            executor.submit(this::dispatchLoop);
        }
    }

    private void dispatchLoop() {
        while (running) {
            try {
                ProcessPaymentRequest request = queue.dequeue();
                if (request != null) {
                    processPayment.execute(request);
                }
            } catch (Exception e) {
                Log.error("Error processing payment request", e);
            }
        }
    }

/*    @PostConstruct
    void onStart() {
        executor = Executors.newVirtualThreadPerTaskExecutor();
        Thread.ofVirtual().start(() -> {
            while (running) {
                try {
                    ProcessPaymentRequest request = queue.dequeue();
                    if (request != null) {
                        executor.submit(() -> processPayment.execute(request));
                    }
                } catch (Exception e) {
                    Log.error("Error polling queue", e);
                }
            }
        });
    }*/

    @PreDestroy
    void onStop() {
        running = false;
    }
}
