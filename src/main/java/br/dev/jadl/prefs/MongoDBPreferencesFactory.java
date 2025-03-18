package br.dev.jadl.prefs;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;

import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import org.bson.Document;

public class MongoDBPreferencesFactory implements PreferencesFactory {

    @Override
    public Preferences systemRoot() {
        return SystemPreference.instance;
    }

    private static final class SystemPreference {
        private static final Preferences instance = preferences("system");
    }

    @Override
    public Preferences userRoot() {
        return UserPreference.instance;
    }

    private static final class UserPreference {
        private static final Preferences instance = preferences("user");
    }

    private static Preferences preferences(final String scope) {

        final ConnectionString cs = new ConnectionString(property("url", scope));

        String collection = cs.getCollection();
        if (collection == null) {
            collection = property("collection", scope);
        }

        MongoCollection<Document> c = MongoClients.create(cs)
            .getDatabase(cs.getDatabase())
            .getCollection(collection);

        return new MongoDBPreferences(c);
    }

    private static String property(final String key, final String scope) {
        final String prefix = MongoDBPreferences.class.getCanonicalName();
        final String scoped = String.format("%s.%s.%s", prefix, scope, key);
        final String unscoped = String.format("%s.%s", prefix, key);
        return System.getProperty(scoped, System.getProperty(unscoped));
    }
}
