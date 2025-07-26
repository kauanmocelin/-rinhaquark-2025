package dev.kauanmocelin.payments.controller;

import dev.kauanmocelin.payments.client.DefaultPaymentProcessorClient;
import dev.kauanmocelin.payments.client.ProcessPaymentRequest;
import dev.kauanmocelin.payments.controller.dto.PaymentRequest;
import dev.kauanmocelin.payments.controller.dto.PaymentSummary;
import dev.kauanmocelin.payments.controller.dto.PaymentSummaryResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestQuery;

import java.math.BigDecimal;
import java.time.Instant;

@Path("")
public class PaymentsResource {

    @RestClient
    DefaultPaymentProcessorClient defaultPaymentProcessorClient;

    @Path("/payments")
    @POST
    public Response paymentToProcess(@Valid final PaymentRequest paymentRequest) {
        return defaultPaymentProcessorClient.processPayment(new ProcessPaymentRequest(
            paymentRequest.correlationId(),
            paymentRequest.amount(),
            Instant.now()
        ));
    }

    @Path("/payments-summary")
    @GET
    public PaymentSummaryResponse getPaymentsSummary(@NotNull @RestQuery Instant from, @NotNull @RestQuery Instant to) {
        return new PaymentSummaryResponse(
            new PaymentSummary(43236, new BigDecimal("415542345.98")),
            new PaymentSummary(423545, new BigDecimal("329347.34"))
        );
    }
}
