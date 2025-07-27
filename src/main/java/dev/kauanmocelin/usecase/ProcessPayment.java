package dev.kauanmocelin.usecase;

import dev.kauanmocelin.payments.client.DefaultPaymentProcessorClient;
import dev.kauanmocelin.payments.client.ProcessPaymentRequest;
import dev.kauanmocelin.payments.controller.dto.PaymentRequest;
import dev.kauanmocelin.repository.Payment;
import dev.kauanmocelin.repository.PaymentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Instant;

@ApplicationScoped
public final class ProcessPayment {

    private final PaymentRepository paymentRepository;
    @RestClient
    DefaultPaymentProcessorClient defaultPaymentProcessorClient;

    public ProcessPayment(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public void execute(final PaymentRequest paymentRequest) {
        final ProcessPaymentRequest processPaymentRequest = new ProcessPaymentRequest(
            paymentRequest.correlationId(),
            paymentRequest.amount(),
            Instant.now()
        );

        defaultPaymentProcessorClient.processPayment(processPaymentRequest);

        paymentRepository.createPayment(new Payment(
            processPaymentRequest.correlationId(),
            processPaymentRequest.amount(),
            processPaymentRequest.requestedAt()
        ));
    }
}
