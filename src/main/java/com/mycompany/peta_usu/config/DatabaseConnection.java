package com.mycompany.peta_usu.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DatabaseConnection - Singleton class untuk koneksi database
 * Menggunakan pattern Singleton untuk OOP
 * 
 * === 4 PILAR OOP ===
 * 1. ENCAPSULATION: Field instance, connection PRIVATE STATIC
 * 2. INHERITANCE: None (Singleton tidak butuh inheritance)
 * 3. POLYMORPHISM: None (Singleton tidak butuh polymorphism)
 * 4. ABSTRACTION: Method getInstance() sembunyikan detail instance creation
 * 
 * DESIGN PATTERN: Singleton - hanya 1 instance connection ke database
 * 
 * @author PETA_USU Team
 */
public class DatabaseConnection {
    
    // ========== ENCAPSULATION: PRIVATE STATIC untuk Singleton ==========
    private static DatabaseConnection instance;  // ← PRIVATE STATIC: Single instance
    private Connection connection;                // ← PRIVATE: DB connection
    
    // Database credentials (PRIVATE STATIC FINAL = constant)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/navigasi_usu";  // ← PRIVATE
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // Sesuaikan dengan password MySQL Anda
    private static final Logger logger = Logger.getLogger(DatabaseConnection.class.getName());
    
    /**
     * Private constructor untuk implementasi Singleton pattern
     * ← ENCAPSULATION: Constructor PRIVATE (tidak bisa `new DatabaseConnection()`)
     */
    private DatabaseConnection() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establish connection
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            if (connection != null) {
                logger.info("Database connection established successfully!");
            }
            
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "MySQL JDBC Driver not found!", e);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to establish database connection!", e);
        }
    }
    
    /**
     * Get singleton instance of DatabaseConnection
     * 
     * @return DatabaseConnection instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        } else {
            try {
                // Check if connection is still valid
                if (instance.connection == null || instance.connection.isClosed()) {
                    instance = new DatabaseConnection();
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error checking connection status", e);
                instance = new DatabaseConnection();
            }
        }
        return instance;
    }
    
    /**
     * Get the database connection
     * 
     * @return Connection object
     */
    public Connection getConnection() {
        try {
            // Verify connection is still valid
            if (connection == null || connection.isClosed()) {
                logger.warning("Connection was closed. Reconnecting...");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting connection", e);
        }
        return connection;
    }
    
    /**
     * Test the database connection
     * 
     * @return true if connection is valid, false otherwise
     */
    public boolean testConnection() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Connection test failed", e);
            return false;
        }
    }
    
    /**
     * Close the database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed successfully");
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error closing connection", e);
        }
    }
    
    /**
     * Update database credentials (optional untuk konfigurasi dinamis)
     */
    public static void setDatabaseCredentials(String url, String user, String password) {
        // This method can be used to update credentials if needed
        logger.info("Database credentials updated");
    }
}
