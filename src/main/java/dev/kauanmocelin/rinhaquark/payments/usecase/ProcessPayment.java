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
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
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
        final ProcessPaymentRequest processPaymentRequest = new ProcessPaymentRequest(
            paymentRequest.correlationId(),
            paymentRequest.amount(),
            Instant.now()
        );
        queue.add(processPaymentRequest);
    }

    //    @CircuitBreaker(
//        requestVolumeThreshold = 5,     // precisa de 5 falhas seguidas
//        failureRatio = 0.5,             // 60% de falhas já dispara
//        delay = 5000,                    // circuito aberto por 5 segundos
//        failOn = TimeoutException.class
//    )
//    @Retry(maxRetries = 10, delay = 100, delayUnit = ChronoUnit.MILLIS)
    @Timeout(value = 500)
    @Fallback(fallbackMethod = "fallbackPayment")
    public void execute(final ProcessPaymentRequest processPaymentRequest) {
        final long startTime = System.nanoTime();
        try {
            defaultPaymentProcessorClient.processPayment(processPaymentRequest);
            paymentRepository.createPayment(new Payment(
                processPaymentRequest.correlationId(),
                processPaymentRequest.amount(),
                processPaymentRequest.requestedAt(),
                PaymentProcessorType.DEFAULT
            ));
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            LOG.infof("✅ API default-processor respondeu em %d ms", durationMs);
        } catch (Exception e) {
//            queue.add(processPaymentRequest);
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            LOG.warnf("❌ API default-processor falhou após %d ms", durationMs, e);
        }
    }

    public void fallbackPayment(final ProcessPaymentRequest processPaymentRequest) {
        LOG.warn("Fallback payment processor called for correlationId: " + processPaymentRequest.correlationId());
        try {
            fallbackPaymentProcessorClient.processPayment(processPaymentRequest);
            paymentRepository.createPayment(new Payment(
                processPaymentRequest.correlationId(),
                processPaymentRequest.amount(),
                processPaymentRequest.requestedAt(),
                PaymentProcessorType.FALLBACK
            ));
        } catch (Exception e) {
            LOG.warnf("Fallback processor also failed", e);
            queue.add(processPaymentRequest);
//          LOG.errorf("❌ Falha ao chamar fallback: %s", t.getMessage(), t);
        }
    }
}
