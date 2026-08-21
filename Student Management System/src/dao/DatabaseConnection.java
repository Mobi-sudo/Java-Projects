package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.File;
import java.util.logging.Logger;
import java.util.logging.Level;

public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    // Defaults (used if no properties or env vars provided)
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/student_management";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "javaproject1234";
    private static final String PROPS_FILE = "db.properties"; // look in working dir

    /**
     * Check whether MySQL JDBC Driver is available on classpath.
     * Logs a SEVERE message if not found.
     */
    public static boolean isDriverAvailable() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return true;
        } catch (ClassNotFoundException cnfe) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC driver not found on classpath. Please add mysql-connector-java.jar to your classpath.", cnfe);
            return false;
        }
    }


    public static Connection getConnection() {
        String url = DEFAULT_URL;
        String user = DEFAULT_USER;
        String password = DEFAULT_PASSWORD;

        try {
            // 1) Try environment variables
            String envUrl = System.getenv("JDBC_URL");
            String envUser = System.getenv("JDBC_USER");
            String envPass = System.getenv("JDBC_PASSWORD");
            if (envUrl != null && !envUrl.isEmpty()) url = envUrl;
            if (envUser != null && !envUser.isEmpty()) user = envUser;
            if (envPass != null && !envPass.isEmpty()) password = envPass;

            // 2) Then try properties file if present (overrides defaults and env)
            File f = new File(PROPS_FILE);
            if (f.exists()) {
                Properties p = new Properties();
                try (FileInputStream fis = new FileInputStream(f)) {
                    p.load(fis);
                }
                url = p.getProperty("jdbc.url", url);
                user = p.getProperty("jdbc.user", user);
                password = p.getProperty("jdbc.password", password);
            }

            // Ensure JDBC driver is loaded (helps when driver not auto-registered)
            if (!isDriverAvailable()) {
                LOGGER.log(Level.WARNING, "JDBC driver is not available. Connection attempts will fail until connector is added to classpath.");
            }

            // Provide clear logging about the attempted URL and user (avoid logging passwords)
            LOGGER.log(Level.FINE, "Attempting DB connection to URL={0} user={1}", new Object[]{url, user});

            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to obtain DB connection. URL=" + url + " user=" + user, e);
            return null;
        }
    }
}
