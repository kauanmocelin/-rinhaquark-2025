package dev.kauanmocelin.payments.controller.client;

import dev.kauanmocelin.payments.client.FallbackPaymentProcessorClient;
import dev.kauanmocelin.payments.client.ProcessPaymentRequest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class FallbackPaymentProcessorClientTest {

    @RestClient
    FallbackPaymentProcessorClient client;

    @Test
    void shouldCallRealPaymentProcessorAPI() {
        ProcessPaymentRequest request = new ProcessPaymentRequest(
            UUID.randomUUID(),
            new BigDecimal("123.45"),
            Instant.now()
        );

        Response response = client.processPayment(request);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).contains("\"message\":\"payment processed successfully\"");
    }
}