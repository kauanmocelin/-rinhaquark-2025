package dev.kauanmocelin.rinhaquark.payments.repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class PaymentRepository implements PanacheMongoRepository<Payment> {

    @Inject
    MongoClient mongoClient;

    public void createPayment(final Payment payment) {
        persist(payment);
    }


    public List<Document> summaryPayments(final Instant from, final Instant to) {
        MongoCollection<Document> collection = mongoClient
            .getDatabase("payments_db")
            .getCollection("payments");

        List<Document> pipeline = Arrays.asList(
            new Document("$match", new Document("requestedAt",
                new Document("$gte", from).append("$lte", to))),
            new Document("$group", new Document("_id", "$paymentProcessorType")
                .append("totalRequests", new Document("$sum", 1))
                .append("totalAmount", new Document("$sum", "$amount"))),
            new Document("$project", new Document("type", "$_id")
                .append("totalRequests", 1)
                .append("totalAmount", 1)
                .append("_id", 0))
        );

        return collection.aggregate(pipeline).into(new ArrayList<>());
    }
}
