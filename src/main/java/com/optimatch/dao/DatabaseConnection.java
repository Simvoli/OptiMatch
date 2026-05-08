package com.optimatch.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages database connections for the OptiMatch application.
 *
 * Each call to {@link #getConnection()} returns a fresh JDBC connection;
 * callers are expected to wrap usage in try-with-resources so the
 * connection is closed deterministically. This avoids the previous
 * "shared singleton + try-with-resources" hazard where one DAO call
 * would silently close the connection used by other threads.
 *
 * Configuration is loaded once from {@code config.properties} (located
 * on the classpath) on first use; {@link #configure(String, String, String)}
 * can override settings programmatically (useful for tests).
 */
public final class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    private static final String CONFIG_FILE = "config.properties";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/optimatch";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static String url;
    private static String user;
    private static String password;
    private static volatile boolean initialized = false;

    private DatabaseConnection() {
    }

    /**
     * Initializes configuration from {@code config.properties}.
     * Idempotent and thread-safe.
     */
    private static synchronized void initialize() {
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
                LOGGER.warning("config.properties not found, using defaults");
                url = DEFAULT_URL;
                user = DEFAULT_USER;
                password = DEFAULT_PASSWORD;
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error loading config.properties", e);
            url = DEFAULT_URL;
            user = DEFAULT_USER;
            password = DEFAULT_PASSWORD;
        }
        initialized = true;
    }

    /**
     * Configures the database connection parameters manually.
     * Overrides settings from {@code config.properties}.
     *
     * @param dbUrl      the database URL
     * @param dbUser     the database username
     * @param dbPassword the database password
     */
    public static synchronized void configure(String dbUrl, String dbUser, String dbPassword) {
        url = dbUrl;
        user = dbUser;
        password = dbPassword;
        initialized = true;
    }

    /**
     * Returns a fresh JDBC connection. The caller must close it,
     * preferably via try-with-resources.
     *
     * @return a new database connection
     * @throws SQLException if a connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        initialize();
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * No-op kept for backwards compatibility.
     * Connections are now per-call and closed by their owners.
     */
    public static void closeConnection() {
        // intentionally empty: connections are owned by the caller
    }

    /**
     * Tests the database connection.
     *
     * @return true if a connection can be established and closed cleanly
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Connection test failed", e);
            return false;
        }
    }
}
