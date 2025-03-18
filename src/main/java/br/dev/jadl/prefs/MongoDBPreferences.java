package br.dev.jadl.prefs;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.ReplaceOptions;

import java.util.ArrayList;
import java.util.prefs.AbstractPreferences;

import org.bson.Document;

public class MongoDBPreferences extends AbstractPreferences {

    private final MongoCollection<Document> collection;

    protected MongoDBPreferences(final MongoCollection<Document> collection) {
        this(null, "", collection);
    }

    private MongoDBPreferences(final AbstractPreferences parent, final String node, final MongoCollection<Document> collection) {
        super(parent, node);
        this.collection = collection;
    }

    @Override
    protected void putSpi(final String key, final String value) {
        final ReplaceOptions upsert = new ReplaceOptions().upsert(true);

        final Document olddoc = document(key);
        final Document newdoc = document(key, value);

        collection.replaceOne(olddoc, newdoc, upsert);
    }

    @Override
    protected String getSpi(final String key) {
        final Document document = collection.find(document(key)).first();
        return (document != null) ? document.getString("value") : null;
    }

    @Override
    protected void removeSpi(final String key) {
        collection.deleteOne(document(key));
    }

    @Override
    protected void removeNodeSpi() {
        collection.deleteMany(new Document("node", this.absolutePath()));
    }

    @Override
    protected String[] keysSpi() {
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            final ArrayList<String> keys = new ArrayList<>();
            while (cursor.hasNext()) {
                keys.add(cursor.next().getString("key"));
            }

            return keys.toArray(String[]::new);
        }
    }

    @Override
    protected String[] childrenNamesSpi() {
        return new String[0];
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

    private Document document(final String key) {
        return new Document().append("node", this.absolutePath()).append("key", key);
    }

    private Document document(final String key, final String value) {
        return document(key).append("value", value);
    }
}
