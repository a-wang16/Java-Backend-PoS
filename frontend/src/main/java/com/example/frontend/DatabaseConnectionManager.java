package com.example.frontend;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


/**
 * This DatabaseConnectionManager manages our connection to our database. It handles using our config.properties,
 * and using those as credentials in order to connect to our databse.
 * @author karlos
 */
public class DatabaseConnectionManager {
    private static Connection connection = null;

    // Private constructor to prevent instantiation
    private DatabaseConnectionManager() {
    }


    /**
     * Provides a global point of access to the database connection.
     * using the properties specified in {@code config.properties}.
     *
     * @return An active database {@link Connection} object.
     * @throws RuntimeException if unable to load database properties or establish a connection.
     */
    public static Connection getConnection() {
        if (connection == null || isConnectionClosed(connection)) {
            synchronized (DatabaseConnectionManager.class) {
                if (connection == null || isConnectionClosed(connection)) {
                    Properties prop = readProperties();
                    if (prop == null) {
                        throw new RuntimeException("Unable to load database properties.");
                    }

                    String databaseName = prop.getProperty("database.name");
                    String databaseUser = prop.getProperty("database.user");
                    String databasePassword = prop.getProperty("database.password");
                    String databaseHost = prop.getProperty("database.host");
                    String databaseUrl = String.format("jdbc:postgresql://%s/%s", databaseHost, databaseName);

                    try {
                        connection = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
                        System.out.println("Successfully connected to the database.");
                    } catch (SQLException e) {
                        System.err.println("Failed to create database connection.");
                        e.printStackTrace();
                    }
                }
            }
        }
        return connection;
    }

    /**
     * Checks if the provided database connection is closed or null.
     *
     * @param conn The database {@link Connection} to check.
     * @return {@code true} if the connection is closed or null, {@code false} otherwise.
     */
    private static boolean isConnectionClosed(Connection conn) {
        try {
            return conn == null || conn.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    /**
     * Reads and loads the database properties from {@code config.properties}
     *
     * @return A {@link Properties} object containing the database connection details.
     * @throws RuntimeException if the properties file cannot be found or an error occurs during loading.
     */
    private static Properties readProperties() {
        Properties prop = new Properties();
        String propFileName = "com/example/frontend/config.properties";

        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(propFileName);
        if (input == null) {
            input = DatabaseConnectionManager.class.getResourceAsStream("/" + propFileName);
        }

        if (input == null) {
            throw new RuntimeException("Unable to find " + propFileName);
        }

        try {
            prop.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Error loading " + propFileName, ex);
        } finally {
            try {
                input.close();
            } catch (IOException ex) {
            }
        }

        return prop;
    }


}
