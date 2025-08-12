package dev.kauanmocelin.rinhaquark.payments.client;

import dev.kauanmocelin.rinhaquark.payments.controller.ProcessPaymentRequest;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/payments")
@RegisterRestClient(configKey = "default-payment-processor-api")
public interface DefaultPaymentProcessorClient {

    @POST
    void processPayment(ProcessPaymentRequest processPaymentRequest);
}