package dev.kauanmocelin.consumer.payment.client;

import dev.kauanmocelin.consumer.payment.ProcessPaymentRequest;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/payments")
@RegisterRestClient(configKey = "default-payment-processor-api")
public interface DefaultPaymentProcessorClient extends PaymentProcessorClient {

    @POST
    void processPayment(ProcessPaymentRequest processPaymentRequest);
}