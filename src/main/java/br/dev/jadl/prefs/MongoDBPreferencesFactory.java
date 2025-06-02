package br.dev.jadl.prefs;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import org.bson.Document;

public class MongoDBPreferencesFactory implements PreferencesFactory {

    private static final Map<String, Preferences> preferences = new HashMap<>();

    @Override
    public Preferences systemRoot() {
        return MongoDBPreferencesFactory.preferences("system");
    }

    @Override
    public Preferences userRoot() {
        return MongoDBPreferencesFactory.preferences("user");
    }

    private static Preferences preferences(final String scope) {

        final String prefix = MongoDBPreferences.class.getCanonicalName();
        final String scoped = String.format("%s.%s.url", prefix, scope);
        final String unscoped = String.format("%s.url", prefix);
        String url = System.getProperty(scoped, System.getProperty(unscoped));

        final ConnectionString cs = new ConnectionString(url);

        MongoCollection<Document> c = MongoClients.create(cs)
            .getDatabase(cs.getDatabase())
            .getCollection(cs.getCollection());

        final String key = String.format("%s:%s", cs.getConnectionString(), scope);

        return preferences.computeIfAbsent(key, k -> new MongoDBPreferences(c));
    }
}
