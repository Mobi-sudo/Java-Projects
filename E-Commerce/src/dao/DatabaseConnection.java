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
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/student_management";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "javaproject1234";
    private static final String PROPS_FILE = "db.properties"; // look in working dir
}
