package br.dev.jadl.prefs;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.Objects;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

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

    private static Preferences preferences(String scope) {
        String prefix = MongoDBPreferences.class.getCanonicalName();

        String url = property(prefix, scope, "url");
        String database = Objects.requireNonNull(new MongoClientURI(url).getDatabase());
        String collection = property(prefix, scope, "collection");

        MongoCollection<Document> c = MongoClients.create(url)
                .getDatabase(database)
                .getCollection(collection);

        return new MongoDBPreferences(c);
    }

    private static String property(String prefix, String scope, String key) {
        String scoped = String.format("%s.%s.%s", prefix, scope, key);
        String unscoped = String.format("%s.%s", prefix, key);
        return System.getProperty(scoped, System.getProperty(unscoped));
    }
}
