# **MongoDB Preferences API**

This is an implementation of the Java Preferences API using MongoDB as the backing storage.
There is another implementation that uses JDBC, which can be found [here](https://github.com/jadler/jdbc-preferences).

## **Features**

- Stores and retrieves preferences in a MongoDB database.
- Configuration via system properties.

## **Technologies Used**

- **Java** (JDK 17 or higher)
- **MongoDB** (MongoDB database)
- **Java Preferences API**

## **How to Set Up and Run**

Add the following dependency to your project:

```xml
<dependency>
    <groupId>br.dev.jadl.preferences</groupId>
    <artifactId>mongodb-preferences</artifactId>
    <version>1.0.0</version>
</dependency>
```

### **Configuration via System Properties**

The configuration follows the same pattern as described in the [jdbc-preferences](https://github.com/jadler/jdbc-preferences) project.

You must configure the MongoDB connection URL and collection through system properties.
These properties can be defined for a specific scope (`user` or `system`). If no scope is defined, the settings will apply to both scopes.

- `br.dev.jadl.prefs.MongoDBPreferences.{scope}.url` - The MongoDB connection URL (e.g., `mongodb://localhost:27017/database`).
- `br.dev.jadl.prefs.MongoDBPreferences.{scope}.collection` - The collection where preferences will be stored.

### **Running Your Application**

You can define the system properties from the command line, through a configuration file, or dynamically at runtime.

#### Running from the Command Line

```bash
# Running a fat JAR directly from the command line
java -Djava.util.prefs.PreferencesFactory=br.dev.jadl.prefs.MongoDBPreferencesFactory \
     -Dbr.dev.jadl.prefs.MongoDBPreferences.user.url=mongodb://localhost/27017/users \
     -Dbr.dev.jadl.prefs.MongoDBPreferences.system.url=mongodb://localhost:27017/system \
     -Dbr.dev.jadl.prefs.MongoDBPreferences.collection=preferences \
     -jar your-application.jar
```

#### From a Configuration File

```text
# Defines the Preferences API implementation to be used
-Djava.util.prefs.PreferencesFactory=br.dev.jadl.prefs.JDBCPreferencesFactory

# No scope defined, so it will be used for both user and system
-Dbr.dev.jadl.prefs.MongoDBPreferences.url=mongodb://localhost:27017/database

# Collection to be used by system preferences
-Dbr.dev.jadl.prefs.MongoDBPreferences.system.collection=system

# Collection to be used by user preferences
-Dbr.dev.jadl.prefs.MongoDBPreferences.user.collection=user
```

```bash
java @<filename> -jar application.jar
```

#### Dynamically at Runtime

```java
import java.util.prefs.Preferences;

import static java.lang.System.Logger.Level.INFO;

public class Main {

    private static final System.Logger logger = System.getLogger(Main.class.getCanonicalName());

    public static void main(String[] args) {

        String prefix = "br.dev.jadl.prefs.MongoDBPreferences";
        System.setProperty(PreferencesFactory.class.getCanonicalName(), String.format("%sFactory", prefix));
        System.setProperty(String.format("%s.user.url", prefix), "mongodb://localhost:27017/database");
        System.setProperty(String.format("%s.user.collection", prefix), "preferences");
        Preferences prefs = Preferences.userRoot().node("theme");

        // Retrieve the stored preference or the default value
        String theme = prefs.get("dark", "my-awesome-theme");

        logger.log(INFO, "Loading {0} theme", theme);
    }
}
```

## **Contributing**

If you encounter any issues or would like to contribute to improving this project, feel free to open an issue or submit a pull request. We welcome your contributions!

## **License**

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

