package dev.kauanmocelin.producer.payments.application;

import dev.kauanmocelin.producer.payments.infra.controller.dto.PaymentRequest;
import dev.kauanmocelin.producer.payments.infra.controller.dto.ProcessPaymentRequest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public final class ProcessPaymentUseCase {

    private final String paymentsQueue;
    private final QueuePublisher queuePublisher;

    public ProcessPaymentUseCase(QueuePublisher queuePublisher,
                                 @ConfigProperty(name = "redis.queue.payments.requests") String paymentsQueue) {
        this.queuePublisher = queuePublisher;
        this.paymentsQueue = paymentsQueue;
    }

    public Uni<Void> registerPayment(final PaymentRequest paymentRequest) {
        return Uni.createFrom().item(() -> {
                final ProcessPaymentRequest processPaymentRequest = new ProcessPaymentRequest(
                    paymentRequest.correlationId(),
                    paymentRequest.amount(),
                    Instant.now().truncatedTo(ChronoUnit.SECONDS)
                );
                queuePublisher.publish(paymentsQueue, processPaymentRequest);
                return null;
            })
            .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
            .replaceWithVoid();
    }
}