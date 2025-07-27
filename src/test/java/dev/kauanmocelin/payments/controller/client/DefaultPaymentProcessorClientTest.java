package dev.kauanmocelin.payments.controller.client;

import dev.kauanmocelin.rinhaquark.payments.client.DefaultPaymentProcessorClient;
import dev.kauanmocelin.rinhaquark.payments.client.ProcessPaymentRequest;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class DefaultPaymentProcessorClientTest {

    @RestClient
    DefaultPaymentProcessorClient client;

    @Test
    void shouldCallRealPaymentProcessorAPI() {
        ProcessPaymentRequest request = new ProcessPaymentRequest(
            UUID.randomUUID(),
            new BigDecimal("123.45"),
            Instant.now()
        );

        final var response = client.processPayment(request);

        assertThat(response).contains("\"message\":\"payment processed successfully\"");
    }
}