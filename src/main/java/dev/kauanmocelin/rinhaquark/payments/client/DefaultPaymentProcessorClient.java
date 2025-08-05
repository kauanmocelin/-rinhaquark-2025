package dev.kauanmocelin.rinhaquark.payments.client;

import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/payments")
@RegisterRestClient(configKey = "default-payment-processor-api")
public interface DefaultPaymentProcessorClient {

    @POST
//    void processPayment(@Valid ProcessPaymentRequest processPaymentRequest);
    Uni<Void> processPayment(@Valid ProcessPaymentRequest processPaymentRequest);
}