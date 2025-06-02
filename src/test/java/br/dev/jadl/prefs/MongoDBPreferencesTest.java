package br.dev.jadl.prefs;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import java.util.UUID;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class MongoDBPreferencesTest extends PreferencesTest {

   private static String prefix = MongoDBPreferences.class.getCanonicalName();

    @Container
    private static final MongoDBContainer container = new MongoDBContainer("mongo:8.0");

    private String url;
    private String collection;

    @BeforeAll
    public static void init() {
        System.setProperty(PreferencesFactory.class.getCanonicalName(), MongoDBPreferencesFactory.class.getCanonicalName());
        container.start();
    }

    @BeforeEach
    public void setup() {
        this.collection = UUID.randomUUID().toString().substring(0, 8);
        this.url = String.format("%s/database.%s", container.getConnectionString(), collection);
        System.setProperty(String.format("%s.url", prefix), url);
    }

    @AfterAll
    public static void teardown() {
        container.stop();
    }

    @Test
    public void test() {

        final String url = container.getConnectionString();

        MongoCollection<Document> c = MongoClients.create(url)
            .getDatabase("database")
            .getCollection(this.collection);

        Preferences prefs = Preferences.userNodeForPackage(this.getClass());

        prefs.node("a").node("b").node("c");
        prefs.node("a").put("test", "test persisted data");

        Assertions.assertAll(
            () -> assertNotNull(c.find(filter("/br/dev/jadl/prefs", null, null)).first()),
            () -> assertNotNull(c.find(filter("/br/dev/jadl/prefs/a", null, null)).first()),
            () -> assertNotNull(c.find(filter("/br/dev/jadl/prefs/a", "test", "test persisted data")).first()),
            () -> assertNotNull(c.find(filter("/br/dev/jadl/prefs/a/b", null, null)).first()),
            () -> assertNotNull(c.find(filter("/br/dev/jadl/prefs/a/b/c", null, null)).first()));
    }

    private static Bson filter(final String node, final String key, final String value) {
        return Filters.and(Filters.eq("node", node), Filters.eq("key", key), Filters.eq("value", value));
    }
}
