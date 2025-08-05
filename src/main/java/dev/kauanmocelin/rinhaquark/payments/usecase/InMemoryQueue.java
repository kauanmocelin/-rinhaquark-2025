package dev.kauanmocelin.rinhaquark.payments.usecase;

import dev.kauanmocelin.rinhaquark.payments.client.ProcessPaymentRequest;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.LinkedList;
import java.util.Queue;

@ApplicationScoped
public class InMemoryQueue {

    private static final Logger LOG = Logger.getLogger(InMemoryQueue.class);
    private final Queue<ProcessPaymentRequest> queue = new LinkedList<>();

    public synchronized void add(ProcessPaymentRequest request) {
        queue.add(request);
//        LOG.infof("📥 Added request to queue. Current queue size: %d", queue.size());
    }

    public synchronized ProcessPaymentRequest poll() {
        ProcessPaymentRequest req = queue.poll();
        if (req != null) {
//            LOG.infof("📤 Consumed request from queue. Remaining: %d", queue.size());
        }
        return req;
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized int size() {
        return queue.size();
    }
}