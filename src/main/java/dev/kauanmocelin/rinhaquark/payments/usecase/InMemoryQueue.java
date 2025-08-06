package dev.kauanmocelin.rinhaquark.payments.usecase;

import dev.kauanmocelin.rinhaquark.payments.client.ProcessPaymentRequest;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@ApplicationScoped
public class InMemoryQueue {

    private static final Logger LOG = Logger.getLogger(InMemoryQueue.class);
    private final BlockingQueue<ProcessPaymentRequest> queue = new LinkedBlockingQueue<>();

    public void add(ProcessPaymentRequest request) {
        queue.add(request);
        LOG.infof("📥 Added request to queue. Current queue size: %d", queue.size());
    }

    public ProcessPaymentRequest take() {
        ProcessPaymentRequest req = null; // Bloqueia até haver item
        try {
            req = queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        LOG.infof("📤 Consumed request from queue. Remaining: %d", queue.size());
        return req;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }
}