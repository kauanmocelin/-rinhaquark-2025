package dev.kauanmocelin.consumer.healthcheck.client;

import dev.kauanmocelin.consumer.healthcheck.HealthCheckResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/payments/service-health")
@RegisterRestClient(configKey = "default-payment-processor-api")
public interface DefaultPaymentProcessorHealthClient {

    @GET
    HealthCheckResponse checkHealth();
}
