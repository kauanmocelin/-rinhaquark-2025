package dev.kauanmocelin.consumer.payment;

import dev.kauanmocelin.consumer.healthcheck.cache.RedisGetOptimalPaymentProcessor;
import dev.kauanmocelin.consumer.payment.client.DefaultPaymentProcessorClient;
import dev.kauanmocelin.consumer.payment.client.FallbackPaymentProcessorClient;
import dev.kauanmocelin.consumer.payment.client.PaymentProcessorClient;
import dev.kauanmocelin.consumer.payment.queue.RedisQueuePublisher;
import dev.kauanmocelin.consumer.payment.repository.PaymentProcessorType;
import dev.kauanmocelin.consumer.payment.repository.PaymentRedisRecord;
import dev.kauanmocelin.consumer.payment.repository.PaymentRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Map;

@ApplicationScoped
public final class ProcessPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final RedisGetOptimalPaymentProcessor redisGetOptimalPaymentProcessor;
    private final RedisQueuePublisher redisQueuePublisher;
    private final String paymentsQueue;
    private final Map<PaymentProcessorType, PaymentProcessorClient> processorMap;

    public ProcessPaymentUseCase(@RestClient DefaultPaymentProcessorClient defaultPaymentProcessorClient,
                                 @RestClient FallbackPaymentProcessorClient fallbackPaymentProcessorClient,
                                 PaymentRepository paymentRepository,
                                 RedisGetOptimalPaymentProcessor redisGetOptimalPaymentProcessor,
                                 RedisQueuePublisher redisQueuePublisher,
                                 @ConfigProperty(name = "redis.queue.payments.requests") String paymentsQueue) {
        this.paymentRepository = paymentRepository;
        this.redisGetOptimalPaymentProcessor = redisGetOptimalPaymentProcessor;
        this.redisQueuePublisher = redisQueuePublisher;
        this.paymentsQueue = paymentsQueue;
        this.processorMap = Map.of(
            PaymentProcessorType.DEFAULT, defaultPaymentProcessorClient,
            PaymentProcessorType.FALLBACK, fallbackPaymentProcessorClient
        );
    }

    public void execute(final ProcessPaymentRequest processPaymentRequest) {
        final int maxRetries = 5;
        final int maxBackoff = 15000;
        long backoffMillis = 1000;

        for (int i = 0; i < maxRetries; i++) {
            try {
                final PaymentProcessorType optimalPaymentProcessorType = redisGetOptimalPaymentProcessor.getOptimalPaymentProcessor();
                final PaymentProcessorClient paymentProcessorClient = processorMap.get(optimalPaymentProcessorType);
                paymentProcessorClient.processPayment(processPaymentRequest);
                final PaymentRedisRecord paymentRedisRecord = new PaymentRedisRecord(
                    processPaymentRequest.correlationId(),
                    processPaymentRequest.amount(),
                    processPaymentRequest.requestedAt(),
                    optimalPaymentProcessorType
                );
                paymentRepository.savePayment(paymentRedisRecord);
                return;
            } catch (Exception e) {
                Log.error(e);
                try {
                    Thread.sleep(backoffMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
                backoffMillis = Math.min(backoffMillis * 2, maxBackoff);
            }
        }
        redisQueuePublisher.publish(paymentsQueue, processPaymentRequest);
    }
}