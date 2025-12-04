package com.mycompany.peta_usu.models;

import java.sql.Timestamp;

/**
 * Marker Model Class
 * Merepresentasikan custom markers yang ditambahkan admin
 * Extends BaseModel untuk Inheritance
 * 
 * @author PETA_USU Team
 */
public class Marker extends BaseModel {
    
    private int markerId;
    private String markerName;
    private String markerType;
    private String description;
    private double latitude;
    private double longitude;
    private String iconPath;
    private String iconName;
    private int createdBy;
    
    // Constructors
    public Marker() {
        super(); // Call parent constructor
    }
    
    public Marker(String markerName, String markerType, double latitude, double longitude) {
        super(); // Call parent constructor
        this.markerName = markerName;
        this.markerType = markerType;
        this.latitude = latitude;
        this.longitude = longitude;
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
    
    // isActive(), createdAt, updatedAt inherited from BaseModel
    
    @Override
    public String toString() {
        return markerName + " (" + markerType + ")";
    }
}
