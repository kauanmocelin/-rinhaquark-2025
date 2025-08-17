package dev.kauanmocelin.rinhaquark.payments.infra;

import dev.kauanmocelin.rinhaquark.payments.repository.Payment;
import dev.kauanmocelin.rinhaquark.payments.repository.PaymentRepository;
import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@ApplicationScoped
public class PaymentBatchService {

    @ConfigProperty(name = "payments.batch.size")
    int batchSize;

    @ConfigProperty(name = "payments.batch.interval_ms")
    long batchIntervalMs;

    @ConfigProperty(name = "payments.threads.number")
    long threadsNumber;

    private final BlockingQueue<Payment> queue = new LinkedBlockingQueue<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ExecutorService worker;

    @Inject
    PaymentRepository paymentRepository;

    @PostConstruct
    void start() {
        scheduler.scheduleAtFixedRate(this::processBatch, 0, batchIntervalMs, TimeUnit.MILLISECONDS);
        worker = Executors.newVirtualThreadPerTaskExecutor();
        for (int i = 0; i < 1; i++) {
            worker.submit(this::processBatch);
        }
    }

    public void enqueuePayment(Payment payment) {
        queue.add(payment);
    }

/*    public void flushBatch() {
        List<Payment> batch = new ArrayList<>();
        queue.drainTo(batch);

        if (!batch.isEmpty()) {
            paymentRepository.createPaymentsBatch(batch);
        }
    }*/

    private void processBatch() {
        List<Payment> batch = new ArrayList<>();
        int queueSize = queue.size(); // tamanho antes de drenar
        queue.drainTo(batch, batchSize);

        if (!batch.isEmpty()) {
            Log.infof("Processing batch of size: %d, queue size before drain: %d", batch.size(), queueSize);

            worker.submit(() -> {
                long start = System.currentTimeMillis();
                try {
                    paymentRepository.createPaymentsBatch(batch);
                } catch (Exception e) {
                    Log.error("Error processing batch", e);
                } finally {
                    long duration = System.currentTimeMillis() - start;
                    Log.infof("Batch of size %d processed in %d ms", batch.size(), duration);
                }
            });
        } else {
            Log.infof("No payments to process, queue size: %d", queueSize);
        }
    }


    @PreDestroy
    void shutdown() {
        scheduler.shutdown();
        worker.shutdown();
    }
}