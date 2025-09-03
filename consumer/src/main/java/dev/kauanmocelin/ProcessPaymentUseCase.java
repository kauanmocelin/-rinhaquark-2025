package dev.kauanmocelin;

import dev.kauanmocelin.consumer.Payment;
import dev.kauanmocelin.consumer.PaymentProcessorType;
import dev.kauanmocelin.consumer.PaymentRepository;
import dev.kauanmocelin.consumer.client.DefaultPaymentProcessorClient;
import dev.kauanmocelin.consumer.client.FallbackPaymentProcessorClient;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public final class ProcessPaymentUseCase {

    @RestClient
    DefaultPaymentProcessorClient defaultPaymentProcessorClient;
    @RestClient
    FallbackPaymentProcessorClient fallbackPaymentProcessorClient;
    @Inject
    PaymentRepository paymentRepository;

    @ConfigProperty(name = "redis.queue.payments.requests")
    private String paymentsQueue;

    public void execute(final ProcessPaymentRequest processPaymentRequest) {
        int maxRetriesDefault = 5;
        int maxRetriesFallback = 3;
        long backoffMillisDefault = 100;
        long maxBackoffDefault = 2000;

        long backoffMillisFallback = 500;
        long maxBackoffFallback = 3000;

        // ==== TENTATIVAS DEFAULT ====
        for (int i = 0; i < maxRetriesDefault; i++) {
            long apiStart = System.currentTimeMillis();
            try {
                defaultPaymentProcessorClient.processPayment(processPaymentRequest);
                long apiEnd = System.currentTimeMillis();

                Payment payment = new Payment(
                    processPaymentRequest.correlationId(),
                    processPaymentRequest.amount(),
                    processPaymentRequest.requestedAt(),
                    PaymentProcessorType.DEFAULT
                );

                long saveStart = System.currentTimeMillis();
                paymentRepository.savePayment(payment);
                long saveEnd = System.currentTimeMillis();

                Log.infof("[DEFAULT] CorrelationId=%s | API time=%d ms | Save time=%d ms | Attempt=%d",
                    processPaymentRequest.correlationId(),
                    (apiEnd - apiStart),
                    (saveEnd - saveStart),
                    i + 1
                );

                return; // sucesso
            } catch (Exception e) {
                long apiEnd = System.currentTimeMillis();
                Log.errorf("[DEFAULT] CorrelationId=%s | Attempt=%d | API time=%d ms | Error: %s",
                    processPaymentRequest.correlationId(),
                    i + 1,
                    (apiEnd - apiStart),
                    e.getMessage()
                );

                try {
                    Thread.sleep(backoffMillisDefault);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
                backoffMillisDefault = Math.min(backoffMillisDefault * 2, maxBackoffDefault);
            }
        }

        // ==== TENTATIVAS FALLBACK ====
        for (int i = 0; i < maxRetriesFallback; i++) {
            long apiStart = System.currentTimeMillis();
            try {
                fallbackPaymentProcessorClient.processPayment(processPaymentRequest);
                long apiEnd = System.currentTimeMillis();

                Payment payment = new Payment(
                    processPaymentRequest.correlationId(),
                    processPaymentRequest.amount(),
                    processPaymentRequest.requestedAt(),
                    PaymentProcessorType.FALLBACK
                );

                long saveStart = System.currentTimeMillis();
                paymentRepository.savePayment(payment);
                long saveEnd = System.currentTimeMillis();

                Log.infof("[FALLBACK] CorrelationId=%s | API time=%d ms | Save time=%d ms | Attempt=%d",
                    processPaymentRequest.correlationId(),
                    (apiEnd - apiStart),
                    (saveEnd - saveStart),
                    i + 1
                );

                return; // sucesso
            } catch (Exception e) {
                long apiEnd = System.currentTimeMillis();
                Log.errorf("[FALLBACK] CorrelationId=%s | Attempt=%d | API time=%d ms | Error: %s",
                    processPaymentRequest.correlationId(),
                    i + 1,
                    (apiEnd - apiStart),
                    e.getMessage()
                );

                try {
                    Thread.sleep(backoffMillisFallback);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
                backoffMillisFallback = Math.min(backoffMillisFallback * 2, maxBackoffFallback);
            }
        }

        // Opcional: aqui poderia re-enfileirar
        // queue.enqueue(processPaymentRequest);
    }


    /*public void execute(final ProcessPaymentRequest processPaymentRequest) {
        int maxRetriesDefault = 5;
        int maxRetriesFallback = 3;
        long backoffMillisDefault = 100;
        long maxBackoffDefault = 2000;

        long backoffMillisFallback = 500;
        long maxBackoffFallback = 3000;

        for (int i = 0; i < maxRetriesDefault; i++) {
            try {
                defaultPaymentProcessorClient.processPayment(processPaymentRequest);
                Payment payment = new Payment(
                    processPaymentRequest.correlationId(),
                    processPaymentRequest.amount(),
                    processPaymentRequest.requestedAt(),
                    PaymentProcessorType.DEFAULT
                );
                paymentRepository.savePayment(payment);
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
                Payment payment = new Payment(
                    processPaymentRequest.correlationId(),
                    processPaymentRequest.amount(),
                    processPaymentRequest.requestedAt(),
                    PaymentProcessorType.FALLBACK
                );
                paymentRepository.savePayment(payment);
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
//        queue.enqueue(processPaymentRequest);
    }*/
}