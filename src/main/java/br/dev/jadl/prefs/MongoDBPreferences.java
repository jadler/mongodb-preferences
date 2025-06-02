package br.dev.jadl.prefs;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReplaceOptions;

import java.util.prefs.AbstractPreferences;
import java.util.stream.StreamSupport;

import org.bson.Document;

public class MongoDBPreferences extends AbstractPreferences {

    private final MongoCollection<Document> collection;

    protected MongoDBPreferences(final MongoCollection<Document> collection) {
        this(null, "", collection);
    }

    private MongoDBPreferences(final AbstractPreferences parent, final String node, final MongoCollection<Document> collection) {
        super(parent, node);
        this.collection = collection;

        this.collection.insertOne(document());
    }

    @Override
    protected void putSpi(final String key, final String value) {

        final Document olddoc = document(key);
        final Document newdoc = document(key, value);
        final ReplaceOptions upsert = new ReplaceOptions().upsert(true);

        this.collection.replaceOne(olddoc, newdoc, upsert);
    }

    @Override
    protected String getSpi(final String key) {
        final Document document = collection.find(document(key)).first();
        return (document != null) ? document.getString("value") : null;
    }

    @Override
    protected void removeSpi(final String key) {
        this.collection.deleteOne(document(key));
    }

    @Override
    protected void removeNodeSpi() {
        this.collection.deleteMany(new Document("node", this.absolutePath()));
    }

    @Override
    protected String[] keysSpi() {
        final FindIterable<Document> projection = this.collection
            .find(Filters.eq("node", this.absolutePath()))
            .projection(Projections.include("key"));

        return StreamSupport.stream(projection.spliterator(), false)
            .map(document -> document.getString("key"))
            .filter(key -> key != null)
            .toArray(String[]::new);
    }

    @Override
    protected String[] childrenNamesSpi() {
        final FindIterable<Document> projection = this.collection
            .find(Filters.regex("node", "^" + this.absolutePath() + "[^/]+$"))
            .projection(Projections.include("node"));

        return StreamSupport.stream(projection.spliterator(), false)
            .map(document -> document.getString("node"))
            .map(document -> document.substring(this.absolutePath().length()))
            .filter(node -> node != null)
            .toArray(String[]::new);
    }

    @Override
    protected AbstractPreferences childSpi(final String node) {
        return new MongoDBPreferences(this, node, this.collection);
    }

    @Override
    protected void syncSpi() {
        // Not needed for MongoDB
    }

    @Override
    protected void flushSpi() {
        // Not needed for  MongoDB
    }

    private Document document() {
        return new Document().append("node", this.absolutePath());
    }

    private Document document(final String key) {
        return document().append("key", key);
    }

    private Document document(final String key, final String value) {
        return document(key).append("value", value);
    }
}
