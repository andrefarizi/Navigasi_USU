package com.mycompany.peta_usu.models;

import java.sql.Timestamp;

/**
 * User Model Class
 * Merepresentasikan user (mahasiswa dan admin)
 * 
 * @author PETA_USU Team
 */
public class User {
    
    private int userId;
    private String nim;
    private String password;
    private String name;
    private String email;
    private UserRole role;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Enum untuk role user
    public enum UserRole {
        ADMIN("admin"),
        USER("user");
        
        private final String value;
        
        UserRole(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static UserRole fromString(String text) {
            for (UserRole role : UserRole.values()) {
                if (role.value.equalsIgnoreCase(text)) {
                    return role;
                }
            }
            return USER;
        }
    }
    
    // Constructors
    public User() {
        this.role = UserRole.USER;
    }
    
    public User(String nim, String password, String name, String email) {
        this.nim = nim;
        this.password = password;
        this.name = name;
        this.email = email;
        this.role = UserRole.USER;
    }
    
    // Getters and Setters
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getNim() {
        return nim;
    }
    
    public void setNim(String nim) {
        this.nim = nim;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }
    
    /**
     * Check if user has permission
     * Polymorphism: Method overloading
     */
    public boolean hasPermission(String permission) {
        if (this.role == UserRole.ADMIN) {
            return true; // Admin has all permissions
        }
        return permission.equals("read"); // User only has read permission
    }
    
    /**
     * Overloaded version - check multiple permissions
     */
    public boolean hasPermission(String... permissions) {
        if (this.role == UserRole.ADMIN) {
            return true;
        }
        for (String permission : permissions) {
            if (!permission.equals("read")) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        return name + " (" + nim + ")";
    }
}
