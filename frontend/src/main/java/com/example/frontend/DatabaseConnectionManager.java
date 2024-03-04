package com.example.frontend;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnectionManager {
    private static Connection connection = null;

    private DatabaseConnectionManager() {
    }

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

    private static boolean isConnectionClosed(Connection conn) {
        try {
            return conn == null || conn.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

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
