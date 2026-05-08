package com.optimatch.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

// connection factory
// RU: каждый getConnection() отдаёт НОВОЕ соединение, владелец обязан закрыть его сам
// (try-with-resources). Раньше был общий singleton, и при закрытии в одном DAO
// другие потоки внезапно теряли коннект
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

    // utility class, no instances
    private DatabaseConnection() {
    }

    // load config.properties once, fall back to defaults if missing
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

    // open a fresh JDBC connection (caller closes it)
    public static Connection getConnection() throws SQLException {
        initialize();
        return DriverManager.getConnection(url, user, password);
    }

    // probe the database, returns false if anything goes wrong
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Connection test failed", e);
            return false;
        }
    }
}
