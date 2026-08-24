package dao;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/ecommerce";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "javaproject1234";
    private static final String PROPS_FILE = "db.properties";

    public static Connection getConnection() {
        Properties props = new Properties();
        try {
            File propsFile = new File(PROPS_FILE);
            if (propsFile.exists()) {
                try (FileInputStream in = new FileInputStream(propsFile)) {
                    props.load(in);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to read db.properties; using environment/default values.", e);
        }

        String url = System.getenv().getOrDefault("DB_URL", props.getProperty("db.url", DEFAULT_URL));
        String user = System.getenv().getOrDefault("DB_USER", props.getProperty("db.user", DEFAULT_USER));
        String password = System.getenv().getOrDefault("DB_PASSWORD", props.getProperty("db.password", DEFAULT_PASSWORD));

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error connecting to the database", e);
            return null;
        }
    }
}
