package com.mycompany.peta_usu.models;

import java.sql.Timestamp;

/**
 * Building Model Class
 * Merepresentasikan gedung-gedung di USU
 * 
 * === 4 PILAR OOP YANG DITERAPKAN ===
 * 
 * 1. ENCAPSULATION (Enkapsulasi):
 *    - Semua atribut (buildingId, buildingName, latitude, dll) PRIVATE
 *    - Akses data hanya lewat getter/setter untuk kontrol keamanan
 *    - Contoh: latitude tidak bisa diset sembarangan nilai invalid
 * 
 * 2. INHERITANCE (Pewarisan):
 *    - Enum BuildingType mewarisi konsep enum dari Java
 *    - Class ini extend Object (implicit, semua class Java extend Object)
 * 
 * 3. POLYMORPHISM (Polimorfisme):
 *    - Override toString() agar print building tampil nama, bukan alamat memori
 *    - Method distanceFrom() bisa dipanggil dengan parameter berbeda
 * 
 * 4. ABSTRACTION (Abstraksi):
 *    - Class ini MODEL murni, tidak peduli datanya dari mana (DB/file/API)
 *    - Method distanceFrom() sembunyikan rumus Haversine yang kompleks
 *    - User tinggal panggil: building.distanceFrom(lat, lng) tanpa tahu rumus
 * 
 * @author PETA_USU Team
 */
public class Building {
    
    private int buildingId;
    private String buildingCode;
    private String buildingName;
    private BuildingType buildingType;
    private String description;
    private double latitude;
    private double longitude;
    private String address;
    private int floorCount;
    private String iconPath;
    private boolean isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Enum untuk tipe gedung
    public enum BuildingType {
        FAKULTAS("fakultas"),
        GEDUNG("gedung"),
        MUSHOLLA("musholla"),
        PERPUSTAKAAN("perpustakaan"),
        STADION("stadion"),
        MASJID("masjid");
        
        private final String value;
        
        BuildingType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static BuildingType fromString(String text) {
            for (BuildingType type : BuildingType.values()) {
                if (type.value.equalsIgnoreCase(text)) {
                    return type;
                }
            }
            return GEDUNG;
        }
    }
    
    // Constructors
    public Building() {
        this.isActive = true;
    }
    
    public Building(String buildingCode, String buildingName, BuildingType buildingType, 
                   double latitude, double longitude) {
        this.buildingCode = buildingCode;
        this.buildingName = buildingName;
        this.buildingType = buildingType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isActive = true;
    }
    
    // Getters and Setters
    public int getBuildingId() {
        return buildingId;
    }
    
    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }
    
    public String getBuildingCode() {
        return buildingCode;
    }
    
    public void setBuildingCode(String buildingCode) {
        this.buildingCode = buildingCode;
    }
    
    public String getBuildingName() {
        return buildingName;
    }
    
    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }
    
    public BuildingType getBuildingType() {
        return buildingType;
    }
    
    public void setBuildingType(BuildingType buildingType) {
        this.buildingType = buildingType;
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
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public int getFloorCount() {
        return floorCount;
    }
    
    public void setFloorCount(int floorCount) {
        this.floorCount = floorCount;
    }
    
    public String getIconPath() {
        return iconPath;
    }
    
    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
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
    // Mengubah behavior default, print "Fakultas Teknik (FT)" bukan "Building@abc123"
    @Override
    public String toString() {
        return buildingName + " (" + buildingCode + ")";
    }
    
    /**
     * Calculate distance from a given point
     * Using Haversine formula
     * 
     * === ABSTRACTION: Sembunyikan kompleksitas rumus Haversine ===
     * User tidak perlu tahu rumus matematika rumit di balik ini
     * Cukup panggil: building.distanceFrom(3.5688, 98.6618)
     * Hasil langsung dapat jarak dalam meter, tanpa repot hitung manual
     */
    public double distanceFrom(double lat, double lng) {
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat - this.latitude);
        double lonDistance = Math.toRadians(lng - this.longitude);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(lat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c * 1000; // convert to meters
    }
}
