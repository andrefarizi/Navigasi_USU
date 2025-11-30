package com.mycompany.peta_usu.models;

import java.sql.Timestamp;

/**
 * Room Model Class
 * Merepresentasikan ruangan/kelas dalam gedung
 * 
 * @author PETA_USU Team
 */
public class Room {
    
    private int roomId;
    private int buildingId;
    private String roomCode;
    private String roomName;
    private int floorNumber;
    private RoomType roomType;
    private int capacity;
    private String description;
    private boolean isAvailable;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Building info (from join)
    private String buildingName;
    
    // Enum untuk tipe ruangan
    public enum RoomType {
        CLASSROOM("classroom"),
        LABORATORY("laboratory"),
        OFFICE("office"),
        AUDITORIUM("auditorium"),
        OTHER("other");
        
        private final String value;
        
        RoomType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static RoomType fromString(String text) {
            for (RoomType type : RoomType.values()) {
                if (type.value.equalsIgnoreCase(text)) {
                    return type;
                }
            }
            return CLASSROOM;
        }
    }
    
    // Constructors
    public Room() {
        this.isAvailable = true;
        this.roomType = RoomType.CLASSROOM;
    }
    
    public Room(int buildingId, String roomCode, String roomName, int floorNumber) {
        this.buildingId = buildingId;
        this.roomCode = roomCode;
        this.roomName = roomName;
        this.floorNumber = floorNumber;
        this.isAvailable = true;
        this.roomType = RoomType.CLASSROOM;
    }
    
    // Getters and Setters
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
    
    public int getBuildingId() {
        return buildingId;
    }
    
    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }
    
    public String getRoomCode() {
        return roomCode;
    }
    
    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }
    
    public String getRoomName() {
        return roomName;
    }
    
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
    
    public int getFloorNumber() {
        return floorNumber;
    }
    
    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }
    
    public RoomType getRoomType() {
        return roomType;
    }
    
    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }
    
    public int getCapacity() {
        return capacity;
    }
    
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isAvailable() {
        return isAvailable;
    }
    
    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
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
    
    public String getBuildingName() {
        return buildingName;
    }
    
    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }
    
    @Override
    public String toString() {
        return roomCode + " - " + roomName + " (Lt. " + floorNumber + ")";
    }
}
