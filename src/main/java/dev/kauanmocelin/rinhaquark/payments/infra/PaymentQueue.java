package dev.kauanmocelin.rinhaquark.payments.infra;

import dev.kauanmocelin.rinhaquark.payments.controller.ProcessPaymentRequest;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@ApplicationScoped
public class PaymentQueue {

    private final BlockingQueue<ProcessPaymentRequest> queue = new LinkedBlockingQueue<>();

    public void enqueue(ProcessPaymentRequest request) {
        queue.add(request);
    }

    public ProcessPaymentRequest dequeue() {
        ProcessPaymentRequest request = null;
        try {
            request = queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return request;
    }
}