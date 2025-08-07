package dev.kauanmocelin.rinhaquark.payments.controller;

import dev.kauanmocelin.rinhaquark.payments.controller.dto.PaymentRequest;
import dev.kauanmocelin.rinhaquark.payments.controller.dto.PaymentSummaryResponse;
import dev.kauanmocelin.rinhaquark.payments.usecase.ProcessPayment;
import dev.kauanmocelin.rinhaquark.payments.usecase.SummaryPayment;
import io.smallrye.mutiny.Uni;
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
    /*public Response processPayment(@Valid final PaymentRequest paymentRequest) {
        processPayment.addQueue(paymentRequest);
        return Response.ok().build();
    }*/
    public Uni<Response> processPayment(@Valid PaymentRequest paymentRequest) {
        return Uni.createFrom().item(() -> {
            processPayment.addQueue(paymentRequest);
            return Response.ok().build();
        });
    }

    @Path("/payments-summary")
    @GET
    public PaymentSummaryResponse getPaymentsSummary(@NotNull @RestQuery Instant from, @NotNull @RestQuery Instant to) {
        return summaryPayment.execute(from, to);
    }
}
