package dev.kauanmocelin.payments.client;

import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/payments")
@RegisterRestClient(configKey = "default-payment-processor-api")
public interface DefaultPaymentProcessorClient {

    @POST
    Response processPayment(@Valid ProcessPaymentRequest processPaymentRequest);
}
