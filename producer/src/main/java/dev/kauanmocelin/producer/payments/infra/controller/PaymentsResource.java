package dev.kauanmocelin.producer.payments.infra.controller;

import dev.kauanmocelin.producer.payments.application.ProcessPaymentUseCase;
import dev.kauanmocelin.producer.payments.application.SummaryPaymentUseCase;
import dev.kauanmocelin.producer.payments.infra.controller.dto.PaymentRequest;
import dev.kauanmocelin.producer.payments.infra.controller.dto.PaymentSummaryResponse;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestQuery;

import java.time.Instant;

@Path("")
public class PaymentsResource {

    private final ProcessPaymentUseCase processPayment;
    private final SummaryPaymentUseCase summaryPaymentUseCase;

    public PaymentsResource(ProcessPaymentUseCase processPayment, SummaryPaymentUseCase summaryPaymentUseCase) {
        this.processPayment = processPayment;
        this.summaryPaymentUseCase = summaryPaymentUseCase;
    }

    @Path("/payments")
    @POST
    @Blocking
    public Uni<Response> processPayment(PaymentRequest paymentRequest) {
        return processPayment.registerPayment(paymentRequest)
            .replaceWith(Response.ok().build());
    }

    @Path("/payments-summary")
    @GET
    public PaymentSummaryResponse getPaymentsSummary(@RestQuery Instant from, @RestQuery Instant to) {
        return summaryPaymentUseCase.execute(from, to);
    }
}
