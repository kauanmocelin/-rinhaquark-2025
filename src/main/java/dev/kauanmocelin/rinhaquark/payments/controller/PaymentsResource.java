package dev.kauanmocelin.rinhaquark.payments.controller;

import dev.kauanmocelin.rinhaquark.payments.controller.dto.PaymentRequest;
import dev.kauanmocelin.rinhaquark.payments.controller.dto.PaymentSummaryResponse;
import dev.kauanmocelin.rinhaquark.payments.usecase.ProcessPayment;
import dev.kauanmocelin.rinhaquark.payments.usecase.SummaryPayment;
import io.smallrye.common.annotation.NonBlocking;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestQuery;

import java.time.Instant;

@Path("")
//@RunOnVirtualThread
public class PaymentsResource {

    private final ProcessPayment processPayment;
    private final SummaryPayment summaryPayment;

    public PaymentsResource(ProcessPayment processPayment, SummaryPayment summaryPayment) {
        this.processPayment = processPayment;
        this.summaryPayment = summaryPayment;
    }

    @Path("/payments")
    @POST
    @NonBlocking
    public Response processPayment(PaymentRequest paymentRequest) {
        processPayment.registerPayment(paymentRequest);
        return Response.ok().build();
    }

    @Path("/payments-summary")
    @GET
    public PaymentSummaryResponse getPaymentsSummary(@RestQuery Instant from, @RestQuery Instant to) {
        return summaryPayment.execute(from, to);
    }

    @Path("/payments-sync")
    @GET
    public Response syncPaymentsDatabase() {
        summaryPayment.sync();
        return Response.ok().build();
    }
}
