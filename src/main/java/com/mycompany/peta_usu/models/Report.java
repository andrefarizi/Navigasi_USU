package com.mycompany.peta_usu.models;

import java.sql.Timestamp;

/**
 * Model class for user reports
 * 
 * === 4 PILAR OOP YANG DITERAPKAN ===
 * 
 * 1. ENCAPSULATION (Enkapsulasi):
 *    - Semua field PRIVATE (reportId, userNim, location, dll)
 *    - Akses data hanya via getter/setter
 *    - Tujuan: Validasi data report sebelum disimpan
 * 
 * 2. INHERITANCE (Pewarisan):
 *    - Enum ReportType mewarisi konsep enum dari Java
 *    - Override toString() di enum untuk display name
 * 
 * 3. POLYMORPHISM (Polimorfisme):
 *    - Enum toString() override untuk tampilkan "Jalan Rusak" bukan "JALAN_RUSAK"
 *    - Method fromString() static polymorphism untuk convert String ke enum
 * 
 * 4. ABSTRACTION (Abstraksi):
 *    - Model abstrak untuk laporan user
 *    - User tidak peduli datanya tersimpan di MySQL, MongoDB, atau file
 *    - Cukup buat new Report() dan panggil setter
 * 
 */
public class Report {
    
    // === ENCAPSULATION: Field PRIVATE ===
    
    private int reportId;
    private String userNim;
    private String userName;
    private String location;
    private double latitude;
    private double longitude;
    private String description;
    private ReportType reportType;
    private Timestamp createdAt;
    private boolean isRead;
    
    /**
     * Enum for report types
     * 
     * === INHERITANCE + POLYMORPHISM ===
     * Enum mewarisi dari java.lang.Enum
     * Override toString() untuk custom display
     */
    public enum ReportType {
        JALAN_RUSAK("Jalan Rusak"),
        JALAN_TERTUTUP("Jalan Tertutup"),
        RAMBU_HILANG("Rambu Hilang"),
        LAINNYA("Lainnya");
        
        private final String displayName;
        
        ReportType(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
        
        public static ReportType fromString(String str) {
            for (ReportType type : values()) {
                if (type.name().equals(str) || type.displayName.equals(str)) {
                    return type;
                }
            }
            return LAINNYA;
        }
    }
    
    // Constructors
    public Report() {
    }
    
    public Report(String userNim, String userName, String location, 
                 double latitude, double longitude, String description, 
                 ReportType reportType) {
        this.userNim = userNim;
        this.userName = userName;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.reportType = reportType;
        this.isRead = false;
    }
    
    // Getters and Setters
    public int getReportId() {
        return reportId;
    }
    
    public void setReportId(int reportId) {
        this.reportId = reportId;
    }
    
    public String getUserNim() {
        return userNim;
    }
    
    public void setUserNim(String userNim) {
        this.userNim = userNim;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public ReportType getReportType() {
        return reportType;
    }
    
    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }
}
