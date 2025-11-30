package com.mycompany.peta_usu.test;

import com.mycompany.peta_usu.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * TestDatabaseConnection - Test koneksi database
 */
public class TestDatabaseConnection {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Database Connection ===");
        
        try {
            // Test koneksi
            DatabaseConnection dbConn = DatabaseConnection.getInstance();
            Connection conn = dbConn.getConnection();
            
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Database connection SUCCESS!");
                
                // Test query users table
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE role = 'admin'");
                
                System.out.println("\n=== Admin Users in Database ===");
                while (rs.next()) {
                    System.out.println("NIM: " + rs.getString("nim"));
                    System.out.println("Password: " + rs.getString("password"));
                    System.out.println("Name: " + rs.getString("name"));
                    System.out.println("Role: " + rs.getString("role"));
                    System.out.println("---");
                }
                
                rs.close();
                stmt.close();
                
            } else {
                System.out.println("❌ Database connection FAILED!");
            }
            
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            System.out.println("\nKemungkinan masalah:");
            System.out.println("1. MySQL tidak running");
            System.out.println("2. Database 'navigasi_usu' belum dibuat");
            System.out.println("3. Password MySQL salah di DatabaseConnection.java");
            System.out.println("4. Table 'users' belum dibuat");
            
            System.out.println("\nSolusi:");
            System.out.println("1. Pastikan MySQL running");
            System.out.println("2. Run command: mysql -u root -p");
            System.out.println("3. CREATE DATABASE navigasi_usu;");
            System.out.println("4. USE navigasi_usu;");
            System.out.println("5. source database/navigasi_usu_schema.sql;");
            
            e.printStackTrace();
        }
    }
}
