package dev.kauanmocelin.rinhaquark.payments.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/payments-sync")
@RegisterRestClient(configKey = "internal-sync-payments-api")
public interface SyncPaymentsClient {

    @GET
    String syncPaymentsDatabase();
}