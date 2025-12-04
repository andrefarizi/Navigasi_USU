package com.mycompany.peta_usu.models;

import java.sql.Timestamp;

/**
 * BaseModel - Abstract base class untuk semua model
 * Implementasi Inheritance & Abstraction
 * 
 * Menyediakan atribut dan method common untuk semua model:
 * - createdAt, updatedAt (timestamp tracking)
 * - isActive (soft delete)
 * 
 * @author PETA_USU Team
 */
public abstract class BaseModel {
    
    protected Timestamp createdAt;
    protected Timestamp updatedAt;
    protected boolean isActive;
    
    /**
     * Constructor default
     */
    public BaseModel() {
        this.isActive = true;
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
    
    // Getters and Setters
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
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    /**
     * Update timestamp saat data dimodifikasi
     */
    public void touch() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
    
    /**
     * Soft delete - tandai sebagai tidak aktif
     */
    public void softDelete() {
        this.isActive = false;
        this.touch();
    }
    
    /**
     * Restore dari soft delete
     */
    public void restore() {
        this.isActive = true;
        this.touch();
    }
    
    /**
     * Abstract method - setiap model harus implement toString()
     * Polymorphism through method overriding
     */
    @Override
    public abstract String toString();
}
