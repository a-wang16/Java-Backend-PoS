package com.example.frontend;

import com.example.frontend.MainSceneController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.io.InputStream;

public class DatabaseConnectionManager {
    private static Connection connection = null;

    private DatabaseConnectionManager() { // Private constructor
    }

    public static Connection getConnection() {
        if (connection == null) {
            synchronized (DatabaseConnectionManager.class) {
                if (connection == null) {
                    try {
                        Properties prop = new Properties();
                        InputStream input = MainSceneController.class.getResourceAsStream("config.properties");
                        prop.load(input);

                        String database_name = prop.getProperty("database.name");
                        String database_user = prop.getProperty("database.user");
                        String database_password = prop.getProperty("database.password");
                        String database_host = prop.getProperty("database.host");
                        String database_url = String.format("jdbc:postgresql://%s/%s", database_host, database_name);

                        connection = DriverManager.getConnection(database_url, database_user, database_password);
                        System.out.println("Successfully connected to the database.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Failed to create database connection.");
                    }
                }
            }
        }
        return connection;
    }
}
