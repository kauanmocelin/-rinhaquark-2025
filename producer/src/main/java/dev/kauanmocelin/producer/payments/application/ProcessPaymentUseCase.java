package dev.kauanmocelin.producer.payments.application;

import dev.kauanmocelin.producer.payments.infra.controller.ProcessPaymentRequest;
import dev.kauanmocelin.producer.payments.infra.controller.dto.PaymentRequest;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;

@ApplicationScoped
public final class ProcessPaymentUseCase {

    @ConfigProperty(name = "redis.queue.payments.requests")
    private String paymentsQueue;

    private final QueuePublisher queuePublisher;

    public ProcessPaymentUseCase(QueuePublisher queuePublisher) {
        this.queuePublisher = queuePublisher;
    }

    public Uni<Void> registerPayment(final PaymentRequest paymentRequest) {
        return Uni.createFrom().item(() -> {
                final ProcessPaymentRequest processPaymentRequest = new ProcessPaymentRequest(
                    paymentRequest.correlationId(),
                    paymentRequest.amount(),
                    Instant.now()
                );
                queuePublisher.publish(paymentsQueue, processPaymentRequest);
                return null;
            })
            .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
//            .invoke(() -> Log.info("payment add to queue"))
            .replaceWithVoid();
    }
}