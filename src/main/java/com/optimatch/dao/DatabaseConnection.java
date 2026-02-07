package com.optimatch.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database connections for the OptiMatch application.
 * Implements a singleton pattern for connection management.
 */
public class DatabaseConnection {

    private static final String CONFIG_FILE = "config.properties";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/optimatch";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static String url;
    private static String user;
    private static String password;
    private static boolean initialized = false;

    private static Connection connection;

    /**
     * Private constructor to prevent instantiation.
     */
    private DatabaseConnection() {
    }

    /**
     * Initializes the database configuration from config.properties.
     * Called automatically on first connection attempt.
     */
    private static void initialize() {
        if (initialized) {
            return;
        }

        Properties props = new Properties();
        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                props.load(input);
                url = props.getProperty("db.url", DEFAULT_URL);
                user = props.getProperty("db.user", DEFAULT_USER);
                password = props.getProperty("db.password", DEFAULT_PASSWORD);
            } else {
                System.err.println("config.properties not found, using defaults");
                url = DEFAULT_URL;
                user = DEFAULT_USER;
                password = DEFAULT_PASSWORD;
            }
        } catch (IOException e) {
            System.err.println("Error loading config.properties: " + e.getMessage());
            url = DEFAULT_URL;
            user = DEFAULT_USER;
            password = DEFAULT_PASSWORD;
        }
        initialized = true;
    }

    /**
     * Configures the database connection parameters manually.
     * Overrides settings from config.properties.
     *
     * @param dbUrl      the database URL
     * @param dbUser     the database username
     * @param dbPassword the database password
     */
    public static void configure(String dbUrl, String dbUser, String dbPassword) {
        url = dbUrl;
        user = dbUser;
        password = dbPassword;
        initialized = true;
        closeConnection();
    }

    /**
     * Gets a connection to the database.
     * Creates a new connection if one doesn't exist or is closed.
     *
     * @return an active database connection
     * @throws SQLException if a connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        initialize();
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }

    /**
     * Closes the current database connection if open.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
            connection = null;
        }
    }

    /**
     * Tests the database connection.
     *
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
}
