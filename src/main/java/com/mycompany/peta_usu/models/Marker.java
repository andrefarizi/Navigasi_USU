package com.mycompany.peta_usu.models;

import java.sql.Timestamp;

/**
 * Marker Model Class
 * Merepresentasikan custom markers yang ditambahkan admin
 * 
 * === 4 PILAR OOP YANG DITERAPKAN ===
 * 
 * 1. ENCAPSULATION (Enkapsulasi):
 *    - Semua field (markerId, markerName, latitude, dll) PRIVATE
 *    - Akses hanya lewat getter/setter untuk kontrol data
 *    - Tujuan: Lindungi data marker dari perubahan tidak sah
 * 
 * 2. INHERITANCE (Pewarisan):
 *    - Class ini extend Object (implicit dari Java)
 *    - Bisa di-extend untuk custom marker types (misal: EventMarker, FacilityMarker)
 * 
 * 3. POLYMORPHISM (Polimorfisme):
 *    - Override toString() untuk tampilkan nama + type marker
 *    - Method isActive() bisa dipanggil untuk filter marker aktif/nonaktif
 * 
 * 4. ABSTRACTION (Abstraksi):
 *    - Model sederhana untuk marker di peta
 *    - User tidak perlu tahu data dari database, JSON, atau API
 *    - Cukup dapat object Marker dengan semua info lengkap
 * 
 * @author PETA_USU Team
 */
public class Marker {
    
    // === ENCAPSULATION: Semua field PRIVATE ===
    // Tidak bisa diakses langsung, harus lewat getter/setter
    
    private int markerId;
    private String markerName;
    private String markerType;
    private String description;
    private double latitude;
    private double longitude;
    private String iconPath;
    private String iconName;
    private int createdBy;
    private boolean isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Constructors
    public Marker() {
        this.isActive = true;
    }
    
    public Marker(String markerName, String markerType, double latitude, double longitude) {
        this.markerName = markerName;
        this.markerType = markerType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isActive = true;
    }
    
    // Getters and Setters
    public int getMarkerId() {
        return markerId;
    }
    
    public void setMarkerId(int markerId) {
        this.markerId = markerId;
    }
    
    public String getMarkerName() {
        return markerName;
    }
    
    public void setMarkerName(String markerName) {
        this.markerName = markerName;
    }
    
    public String getMarkerType() {
        return markerType;
    }
    
    public void setMarkerType(String markerType) {
        this.markerType = markerType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public String getIconPath() {
        return iconPath;
    }
    
    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }
    
    public String getIconName() {
        return iconName;
    }
    
    public void setIconName(String iconName) {
        this.iconName = iconName;
    }
    
    public int getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean isActive) {
        this.isActive = isActive;
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
    
    // === POLYMORPHISM: Override toString() ===
    // Tampilkan "Kantin FT (Building)" bukan "Marker@abc123"
    @Override
    public String toString() {
        return markerName + " (" + markerType + ")";
    }
}
