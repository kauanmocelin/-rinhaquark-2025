package dev.kauanmocelin.payments.controller;

import dev.kauanmocelin.payments.controller.dto.PaymentRequest;
import dev.kauanmocelin.payments.controller.dto.PaymentSummaryResponse;
import dev.kauanmocelin.usecase.ProcessPayment;
import dev.kauanmocelin.usecase.SummaryPayment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestQuery;

import java.time.Instant;

@Path("")
public class PaymentsResource {

    private final ProcessPayment processPayment;
    private final SummaryPayment summaryPayment;

    public PaymentsResource(ProcessPayment processPayment, SummaryPayment summaryPayment) {
        this.processPayment = processPayment;
        this.summaryPayment = summaryPayment;
    }

    @Path("/payments")
    @POST
    public Response processPayment(@Valid final PaymentRequest paymentRequest) {
        processPayment.execute(paymentRequest);
        return Response.noContent().build();
    }

    @Path("/payments-summary")
    @GET
    public PaymentSummaryResponse getPaymentsSummary(@NotNull @RestQuery Instant from, @NotNull @RestQuery Instant to) {
        return summaryPayment.execute(from, to);
    }
}
