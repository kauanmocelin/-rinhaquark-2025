package dev.kauanmocelin.rinhaquark.payments.client;

import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/payments")
@RegisterRestClient(configKey = "fallback-payment-processor-api")
public interface FallbackPaymentProcessorClient {

    @POST
    void processPayment(@Valid ProcessPaymentRequest processPaymentRequest);
}
