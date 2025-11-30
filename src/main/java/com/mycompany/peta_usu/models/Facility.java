package com.mycompany.peta_usu.models;

import java.sql.Timestamp;

/**
 * Model untuk tabel facilities
 */
public class Facility {
    private int facilityId;
    private int buildingId;
    private String facilityName;
    private FacilityType facilityType;
    private String description;
    private boolean isAvailable;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // For display join
    private String buildingName;
    
    public enum FacilityType {
        TOILET("toilet", "Toilet"),
        WIFI("wifi", "WiFi"),
        ATM("atm", "ATM"),
        CANTEEN("canteen", "Kantin"),
        LIBRARY("library", "Perpustakaan"),
        LAB("lab", "Laboratorium"),
        MOSQUE("mosque", "Musholla"),
        PARKING("parking", "Parkir"),
        PHOTOCOPY("photocopy", "Fotokopi"),
        CLINIC("clinic", "Klinik"),
        HALL("hall", "Aula"),
        OTHER("other", "Lainnya");
        
        private final String value;
        private final String displayName;
        
        FacilityType(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
        
        public static FacilityType fromString(String text) {
            for (FacilityType ft : FacilityType.values()) {
                if (ft.value.equalsIgnoreCase(text)) {
                    return ft;
                }
            }
            return OTHER;
        }
    }
    
    public Facility() {}
    
    public Facility(int buildingId, String facilityName, FacilityType facilityType) {
        this.buildingId = buildingId;
        this.facilityName = facilityName;
        this.facilityType = facilityType;
        this.isAvailable = true;
    }
    
    // Getters and Setters
    public int getFacilityId() { return facilityId; }
    public void setFacilityId(int facilityId) { this.facilityId = facilityId; }
    
    public int getBuildingId() { return buildingId; }
    public void setBuildingId(int buildingId) { this.buildingId = buildingId; }
    
    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }
    
    public FacilityType getFacilityType() { return facilityType; }
    public void setFacilityType(FacilityType facilityType) { this.facilityType = facilityType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean isAvailable) { this.isAvailable = isAvailable; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    
    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
    
    @Override
    public String toString() {
        return facilityName + " (" + facilityType.getDisplayName() + ")";
    }
}
