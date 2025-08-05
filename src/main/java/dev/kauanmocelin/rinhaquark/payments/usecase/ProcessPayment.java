package dev.kauanmocelin.rinhaquark.payments.usecase;

import dev.kauanmocelin.rinhaquark.payments.client.DefaultPaymentProcessorClient;
import dev.kauanmocelin.rinhaquark.payments.client.FallbackPaymentProcessorClient;
import dev.kauanmocelin.rinhaquark.payments.client.ProcessPaymentRequest;
import dev.kauanmocelin.rinhaquark.payments.controller.dto.PaymentRequest;
import dev.kauanmocelin.rinhaquark.payments.repository.Payment;
import dev.kauanmocelin.rinhaquark.payments.repository.PaymentProcessorType;
import dev.kauanmocelin.rinhaquark.payments.repository.PaymentRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

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

    public Uni<Response> addQueue(final PaymentRequest paymentRequest) {
        final ProcessPaymentRequest processPaymentRequest = new ProcessPaymentRequest(
            paymentRequest.correlationId(),
            paymentRequest.amount(),
            Instant.now()
        );
        queue.add(processPaymentRequest);
        return Uni.createFrom().item(Response.accepted().build());
    }

    @Timeout(value = 105)
    @Retry(maxRetries = 5, delay = 500, delayUnit = ChronoUnit.MILLIS)
    public Uni<Void> execute(final ProcessPaymentRequest processPaymentRequest) {
        final long startTime = System.nanoTime();
        return defaultPaymentProcessorClient.processPayment(processPaymentRequest)
            .invoke(() -> {
                long durationMs = (System.nanoTime() - startTime) / 1_000_000;
                LOG.infof("✅ API default-processor respondeu em %d ms", durationMs);
            })
            .onItem().call(() ->
                paymentRepository.createPayment(new Payment(
                    processPaymentRequest.correlationId(),
                    processPaymentRequest.amount(),
                    processPaymentRequest.requestedAt(),
                    PaymentProcessorType.DEFAULT
                ))
            )
            .onFailure().recoverWithUni(t -> {
                long durationMs = (System.nanoTime() - startTime) / 1_000_000;
                LOG.warnf("❌ API default-processor falhou após %d ms", durationMs, t);
                queue.add(processPaymentRequest);
                //return fallbackPayment(processPaymentRequest);
                return null;
            });
    }


    //    @CircuitBreaker(
    //        requestVolumeThreshold = 2,   // número mínimo de chamadas antes de ativar o CB
    //        failureRatio = 0.5,           // se 50% das últimas chamadas falharem, abre o circuito
    //        delay = 10000,                 // tempo em ms que o circuito fica aberto antes de tentar fechar
    //        successThreshold = 1          // número de chamadas bem sucedidas para fechar o circuito
    //    )
//        @Retry(maxRetries = 3, delay = 100)*/
    @Timeout(value = 1000)
    public Uni<Void> fallbackPayment(final ProcessPaymentRequest processPaymentRequest) {
        LOG.warn("Fallback payment processor called for correlationId: " + processPaymentRequest.correlationId());
        return fallbackPaymentProcessorClient.processPayment(processPaymentRequest)
            .onItem().call(() ->
                paymentRepository.createPayment(new Payment(
                    processPaymentRequest.correlationId(),
                    processPaymentRequest.amount(),
                    processPaymentRequest.requestedAt(),
                    PaymentProcessorType.FALLBACK
                ))
            )
            .onFailure().invoke(t -> {
                LOG.error("Fallback processor also failed", t);
            })
            .onFailure().recoverWithNull();
    }
}
