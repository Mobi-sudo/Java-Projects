package dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/ecommerce";
    private static final String DEFAULT_USER = "root";
    private static final String PROPS_FILE = "db.properties";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Properties props = loadProperties();
        String url = getSetting("DB_URL", props, "db.url", DEFAULT_URL);
        String user = getSetting("DB_USER", props, "db.user", DEFAULT_USER);
        String password = getSetting("DB_PASSWORD", props, "db.password", "");
        return DriverManager.getConnection(url, user, password);
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        File propsFile = new File(PROPS_FILE);
        if (!propsFile.isFile()) {
            return props;
        }
        try (FileInputStream in = new FileInputStream(propsFile)) {
            props.load(in);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to read db.properties; using environment/default values.", e);
        }
        return props;
    }

    private static String getSetting(String environmentName, Properties props, String propertyName, String defaultValue) {
        String environmentValue = System.getenv(environmentName);
        return environmentValue != null ? environmentValue : props.getProperty(propertyName, defaultValue);
    }
}
