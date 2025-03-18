package br.dev.jadl.prefs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class MongoDBPreferencesTest {

    private static String prefix = MongoDBPreferences.class.getCanonicalName();

    @Container
    private static MongoDBContainer container = new MongoDBContainer("mongo:8.0");
    private static String url;

    @BeforeAll
    public static void init() {
        System.setProperty(PreferencesFactory.class.getCanonicalName(), MongoDBPreferencesFactory.class.getCanonicalName());
    }

    @BeforeEach
    public void setup() {
        String collection = UUID.randomUUID().toString().substring(0, 8);
        url = String.format("%s/database.%s", container.getConnectionString(), collection);
        System.setProperty(String.format("%s.url", prefix), url);
    }

    @AfterAll
    public static void teardown() {
        container.stop();
    }

    @Test
    @DisplayName("Removes the preference node and all of its descendants, invalidating any preferences contained in the removed nodes.")
    public final void testRemoveNode() {
        final Preferences root = Preferences.userRoot();
        final Preferences parent = root.node("parent");
        final Preferences child = parent.node("child");

        final Boolean exists = Assertions.assertDoesNotThrow(() -> root.nodeExists(parent.absolutePath()));
        Assertions.assertTrue(exists);

        Assertions.assertDoesNotThrow(() -> parent.removeNode());

        Assertions.assertAll(
                () -> Assertions.assertFalse(root.nodeExists(parent.absolutePath())),
                () -> Assertions.assertFalse(root.nodeExists(child.absolutePath())));
    }

    @ParameterizedTest
    @ValueSource(strings = { "user", "system" })
    @DisplayName("Assert that the document represents all of the preferences contained in this node and all of its descendants.")
    public final void testExportNode(final String scope) {

        final String tree = """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <!DOCTYPE preferences SYSTEM "http://java.sun.com/dtd/preferences.dtd">
                <preferences EXTERNAL_XML_VERSION="1.0">
                  <root type="%s">
                    <map/>
                    <node name="parent">
                      <map/>
                      <node name="child">
                        <map/>
                        <node name="deeper">
                          <map/>
                          <node name="khazad-dum">
                            <map/>
                          </node>
                        </node>
                      </node>
                    </node>
                  </root>
                </preferences>
                """.formatted(scope);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
        final Preferences prefs = "user".equals(scope) ? Preferences.userRoot() : Preferences.systemRoot();

        prefs.node("parent").node("child").node("deeper").node("khazad-dum");

        Assertions.assertDoesNotThrow(() -> prefs.exportSubtree(baos));
        Assertions.assertArrayEquals(tree.getBytes(), baos.toByteArray());
    }

    @Test
    @DisplayName("Imports all of the preferences represented by the XML document on the specified input stream")
    public final void testImportPreferences() throws Exception {

        final String tree = """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <!DOCTYPE preferences SYSTEM "http://java.sun.com/dtd/preferences.dtd">
                <preferences EXTERNAL_XML_VERSION="1.0">
                  <root type="user">
                    <map/>
                    <node name="parent">
                      <map/>
                      <node name="child">
                        <map/>
                      </node>
                    </node>
                    <node name="uncle">
                      <map/>
                    </node>
                  </root>
                </preferences>
                """;

        final ByteArrayInputStream bais = new ByteArrayInputStream(tree.getBytes());
        Assertions.assertDoesNotThrow(() -> Preferences.importPreferences(bais));
        final Preferences prefs = Preferences.userRoot();

        Assertions.assertAll(
                () -> Assertions.assertTrue(prefs.nodeExists("parent")),
                () -> Assertions.assertTrue(prefs.nodeExists("parent/child")),
                () -> Assertions.assertTrue(prefs.nodeExists("uncle")));
    }

    @Test
    public final void testByteArrayStorage() {
        final Preferences prefs = Preferences.userNodeForPackage(MongoDBPreferences.class);

        final byte[] cname = MongoDBPreferences.class.getCanonicalName().getBytes();
        prefs.putByteArray("cname", cname);
        final byte[] actual = prefs.getByteArray("cname", null);

        Assertions.assertArrayEquals(cname, actual);
    }
}
