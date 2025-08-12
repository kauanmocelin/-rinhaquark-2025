package dev.kauanmocelin.rinhaquark.payments.usecase;

import dev.kauanmocelin.rinhaquark.payments.client.DefaultPaymentProcessorClient;
import dev.kauanmocelin.rinhaquark.payments.client.FallbackPaymentProcessorClient;
import dev.kauanmocelin.rinhaquark.payments.controller.ProcessPaymentRequest;
import dev.kauanmocelin.rinhaquark.payments.controller.dto.PaymentRequest;
import dev.kauanmocelin.rinhaquark.payments.infra.PaymentBatchService;
import dev.kauanmocelin.rinhaquark.payments.infra.PaymentQueue;
import dev.kauanmocelin.rinhaquark.payments.repository.Payment;
import dev.kauanmocelin.rinhaquark.payments.repository.PaymentProcessorType;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Instant;

@ApplicationScoped
public final class ProcessPayment {

    private final PaymentQueue queue;
    private final PaymentBatchService paymentBatchService;
    @RestClient
    DefaultPaymentProcessorClient defaultPaymentProcessorClient;
    @RestClient
    FallbackPaymentProcessorClient fallbackPaymentProcessorClient;


    public ProcessPayment(PaymentQueue queue, PaymentBatchService paymentBatchService) {
        this.queue = queue;
        this.paymentBatchService = paymentBatchService;
    }

    public void registerPayment(final PaymentRequest paymentRequest) {
        final ProcessPaymentRequest processPaymentRequest = new ProcessPaymentRequest(
            paymentRequest.correlationId(),
            paymentRequest.amount(),
            Instant.now()
        );
        queue.enqueue(processPaymentRequest);
    }

    public void execute(final ProcessPaymentRequest processPaymentRequest) {
        int maxRetriesDefault = 8;
        int maxRetriesFallback = 2;
        long backoffMillisDefault = 100;
        long maxBackoffDefault = 2000;

        long backoffMillisFallback = 500;
        long maxBackoffFallback = 3000;

        for (int i = 0; i < maxRetriesDefault; i++) {
            try {
                defaultPaymentProcessorClient.processPayment(processPaymentRequest);
                paymentBatchService.enqueuePayment(new Payment(
                    processPaymentRequest.correlationId(),
                    processPaymentRequest.amount(),
                    processPaymentRequest.requestedAt(),
                    PaymentProcessorType.DEFAULT
                ));
                return;
            } catch (Exception e) {
                try {
                    Thread.sleep(backoffMillisDefault);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
                backoffMillisDefault = Math.min(backoffMillisDefault * 2, maxBackoffDefault);
            }
        }

        for (int i = 0; i < maxRetriesFallback; i++) {
            try {
                fallbackPaymentProcessorClient.processPayment(processPaymentRequest);
                paymentBatchService.enqueuePayment(new Payment(
                    processPaymentRequest.correlationId(),
                    processPaymentRequest.amount(),
                    processPaymentRequest.requestedAt(),
                    PaymentProcessorType.FALLBACK
                ));
                return;
            } catch (Exception e) {
                try {
                    Thread.sleep(backoffMillisFallback);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
                backoffMillisFallback = Math.min(backoffMillisFallback * 2, maxBackoffFallback);
            }
        }
        queue.enqueue(processPaymentRequest);
    }
}