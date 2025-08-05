package dev.kauanmocelin.payments.controller.client;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class FallbackPaymentProcessorClientTest {

/*    @RestClient
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
    }*/
}