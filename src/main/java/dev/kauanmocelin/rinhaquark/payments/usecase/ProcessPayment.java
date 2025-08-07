package dev.kauanmocelin.rinhaquark.payments.usecase;

import dev.kauanmocelin.rinhaquark.payments.client.DefaultPaymentProcessorClient;
import dev.kauanmocelin.rinhaquark.payments.client.FallbackPaymentProcessorClient;
import dev.kauanmocelin.rinhaquark.payments.client.ProcessPaymentRequest;
import dev.kauanmocelin.rinhaquark.payments.controller.dto.PaymentRequest;
import dev.kauanmocelin.rinhaquark.payments.repository.Payment;
import dev.kauanmocelin.rinhaquark.payments.repository.PaymentProcessorType;
import dev.kauanmocelin.rinhaquark.payments.repository.PaymentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.Instant;

@ApplicationScoped
public final class ProcessPayment {

    private static final Logger LOG = Logger.getLogger(ProcessPayment.class);

    private final PaymentRepository paymentRepository;
    @RestClient
    DefaultPaymentProcessorClient defaultPaymentProcessorClient;
    @RestClient
    FallbackPaymentProcessorClient fallbackPaymentProcessorClient;
    @Inject
    InMemoryQueue queue;

    public ProcessPayment(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public void addQueue(final PaymentRequest paymentRequest) {
        long start = System.nanoTime();

        final ProcessPaymentRequest processPaymentRequest = new ProcessPaymentRequest(
            paymentRequest.correlationId(),
            paymentRequest.amount(),
            Instant.now()
        );
        queue.add(processPaymentRequest);

        long end = System.nanoTime();
        long durationMs = (end - start) / 1_000_000;

        if (durationMs > 10) {
//            LOG.warnf("⚠️ addQueue took %d ms for correlationId=%s", durationMs, paymentRequest.correlationId());
        }
    }

    //    @Retry(maxRetries = 10, delay = 100, delayUnit = ChronoUnit.MILLIS)
//    @Timeout(value = 200)
    public void execute(final ProcessPaymentRequest processPaymentRequest) {
        final long startTime = System.nanoTime();
        for (int i = 1; i <= 15; i++) {
            try {
                defaultPaymentProcessorClient.processPayment(processPaymentRequest);
                paymentRepository.createPayment(new Payment(
                    processPaymentRequest.correlationId(),
                    processPaymentRequest.amount(),
                    processPaymentRequest.requestedAt(),
                    PaymentProcessorType.DEFAULT
                ));
                long durationMs = (System.nanoTime() - startTime) / 1_000_000;
//                LOG.infof("✅ API default-processor respondeu em %d ms", durationMs);
                return;
            } catch (Exception e) {
                long durationMs = (System.nanoTime() - startTime) / 1_000_000;
//                LOG.warnf("❌ API default-processor falhou após %d ms", durationMs, e);
            }
        }

        try {
//            LOG.warn("Fallback payment processor called for correlationId: " + processPaymentRequest.correlationId());
            fallbackPaymentProcessorClient.processPayment(processPaymentRequest);
            paymentRepository.createPayment(new Payment(
                processPaymentRequest.correlationId(),
                processPaymentRequest.amount(),
                processPaymentRequest.requestedAt(),
                PaymentProcessorType.FALLBACK
            ));
            return;
        } catch (Exception e) {
//            LOG.warnf("Fallback processor also failed", e);
        }

        queue.add(processPaymentRequest);
    }
}
