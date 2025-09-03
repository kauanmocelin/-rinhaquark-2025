package dev.kauanmocelin.consumer.client;

import dev.kauanmocelin.consumer.HealthCheckResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/payments/service-health")
@RegisterRestClient(configKey = "default-payment-processor-api")
public interface DefaultPaymentProcessorHealthClient {

    @GET
    HealthCheckResponse checkHealth();
}
