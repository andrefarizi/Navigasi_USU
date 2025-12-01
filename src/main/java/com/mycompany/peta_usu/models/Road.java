package com.mycompany.peta_usu.models;

import java.sql.Timestamp;

/**
 * Model untuk tabel roads
 */
public class Road {
    private int roadId;
    private String roadName;
    private RoadType roadType;
    private double startLat;
    private double startLng;
    private double endLat;
    private double endLng;
    private boolean isOneWay;
    private double distance;
    private String description;
    private String polylinePoints; // Encoded polyline from Google Maps
    private String googleRoadName; // Road name from Google Maps API (e.g., "Jl. Alumni")
    private String roadSegments; // JSON array of road segments
    private Timestamp lastGmapsUpdate; // Last Google Maps data fetch
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    public enum RoadType {
        MAIN("main", "Jalan Utama"),
        SECONDARY("secondary", "Jalan Sekunder"),
        PEDESTRIAN("pedestrian", "Jalan Pejalan Kaki");
        
        private final String value;
        private final String displayName;
        
        RoadType(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
        
        public static RoadType fromString(String text) {
            if (text == null) return MAIN;
            for (RoadType rt : RoadType.values()) {
                if (rt.value.equalsIgnoreCase(text)) {
                    return rt;
                }
            }
            return MAIN;
        }
    }
    
    public Road() {}
    
    public Road(int roadId, String roadName, RoadType roadType, double startLat, 
                double startLng, double endLat, double endLng) {
        this.roadId = roadId;
        this.roadName = roadName;
        this.roadType = roadType;
        this.startLat = startLat;
        this.startLng = startLng;
        this.endLat = endLat;
        this.endLng = endLng;
    }
    
    // Getters and Setters
    public int getRoadId() { return roadId; }
    public void setRoadId(int roadId) { this.roadId = roadId; }
    
    public String getRoadName() { return roadName; }
    public void setRoadName(String roadName) { this.roadName = roadName; }
    
    public RoadType getRoadType() { return roadType; }
    public void setRoadType(RoadType roadType) { this.roadType = roadType; }
    
    public double getStartLat() { return startLat; }
    public void setStartLat(double startLat) { this.startLat = startLat; }
    
    public double getStartLng() { return startLng; }
    public void setStartLng(double startLng) { this.startLng = startLng; }
    
    public double getEndLat() { return endLat; }
    public void setEndLat(double endLat) { this.endLat = endLat; }
    
    public double getEndLng() { return endLng; }
    public void setEndLng(double endLng) { this.endLng = endLng; }
    
    public boolean isOneWay() { return isOneWay; }
    public void setOneWay(boolean isOneWay) { this.isOneWay = isOneWay; }
    
    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getPolylinePoints() { return polylinePoints; }
    public void setPolylinePoints(String polylinePoints) { this.polylinePoints = polylinePoints; }
    
    public String getGoogleRoadName() { return googleRoadName; }
    public void setGoogleRoadName(String googleRoadName) { this.googleRoadName = googleRoadName; }
    
    public String getRoadSegments() { return roadSegments; }
    public void setRoadSegments(String roadSegments) { this.roadSegments = roadSegments; }
    
    public Timestamp getLastGmapsUpdate() { return lastGmapsUpdate; }
    public void setLastGmapsUpdate(Timestamp lastGmapsUpdate) { this.lastGmapsUpdate = lastGmapsUpdate; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return roadName + " (" + roadType.getDisplayName() + ")";
    }
}
