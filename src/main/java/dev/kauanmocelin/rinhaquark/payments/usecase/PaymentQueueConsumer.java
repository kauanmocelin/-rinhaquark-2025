package dev.kauanmocelin.rinhaquark.payments.usecase;

import dev.kauanmocelin.rinhaquark.payments.client.ProcessPaymentRequest;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.concurrent.atomic.AtomicInteger;

@Startup
@ApplicationScoped
public class PaymentQueueConsumer {

    @Inject
    InMemoryQueue queue;

    @Inject
    ProcessPayment processPayment;

    private static final Logger LOG = Logger.getLogger(PaymentQueueConsumer.class);

    void onStart(@Observes StartupEvent ev) {
        int threadCount = 10;

        for (int i = 0; i < threadCount; i++) {
            Thread consumerThread = new Thread(this::consumeLoop, "queue-consumer-" + i);
            consumerThread.setDaemon(true);
            consumerThread.start();
        }
    }


    private void consumeLoop() {
        AtomicInteger consumed = new AtomicInteger(0);

        while (true) {
            ProcessPaymentRequest request = queue.take();

            if (request != null) {
                processPayment.execute(request);
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
