package dev.kauanmocelin.consumer.client;

import dev.kauanmocelin.consumer.HealthCheckResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/payments/service-health")
@RegisterRestClient(configKey = "fallback-payment-processor-api")
public interface FallbackPaymentProcessorHealthClient {

    @GET
    HealthCheckResponse checkHealth();
}
