package dev.kauanmocelin.rinhaquark.payments;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.bson.Document;

@ApplicationScoped
public class MongoIndexInitializer {

    @Inject
    MongoClient mongoClient;

    public void onStart(@Observes StartupEvent ev) {
        MongoDatabase database = mongoClient.getDatabase("payments_db");
        MongoCollection<Document> collection = database.getCollection("payments");

        collection.createIndex(Indexes.ascending("requestedAt"));

    }
}