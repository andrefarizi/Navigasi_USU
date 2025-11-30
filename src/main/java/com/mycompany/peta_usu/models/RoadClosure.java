package com.mycompany.peta_usu.models;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * RoadClosure Model Class
 * Merepresentasikan penutupan jalan di area USU
 * 
 * @author PETA_USU Team
 */
public class RoadClosure {
    
    private int closureId;
    private int roadId;
    private ClosureType closureType;
    private String reason;
    private Date startDate;
    private Date endDate;
    private Time startTime;
    private Time endTime;
    private boolean isActive;
    private int createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Road details (from join)
    private String roadName;
    private String createdByName;
    
    // Enum untuk tipe penutupan
    public enum ClosureType {
        TEMPORARY("temporary"),
        PERMANENT("permanent"),
        ONE_WAY("one_way");
        
        private final String value;
        
        ClosureType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static ClosureType fromString(String text) {
            for (ClosureType type : ClosureType.values()) {
                if (type.value.equalsIgnoreCase(text)) {
                    return type;
                }
            }
            return TEMPORARY;
        }
    }
    
    // Constructors
    public RoadClosure() {
        this.isActive = true;
    }
    
    public RoadClosure(int roadId, ClosureType closureType, String reason) {
        this.roadId = roadId;
        this.closureType = closureType;
        this.reason = reason;
        this.isActive = true;
    }
    
    // Getters and Setters
    public int getClosureId() {
        return closureId;
    }
    
    public void setClosureId(int closureId) {
        this.closureId = closureId;
    }
    
    public int getRoadId() {
        return roadId;
    }
    
    public void setRoadId(int roadId) {
        this.roadId = roadId;
    }
    
    public ClosureType getClosureType() {
        return closureType;
    }
    
    public void setClosureType(ClosureType closureType) {
        this.closureType = closureType;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public Date getStartDate() {
        return startDate;
    }
    
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    public Date getEndDate() {
        return endDate;
    }
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    public Time getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }
    
    public Time getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    public int getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
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
    
    public String getRoadName() {
        return roadName;
    }
    
    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }
    
    public String getCreatedByName() {
        return createdByName;
    }
    
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
    
    /**
     * Check if closure is currently active based on date/time
     */
    public boolean isCurrentlyActive() {
        if (!isActive) return false;
        
        java.util.Date now = new java.util.Date();
        
        // Check date range
        if (startDate != null && now.before(startDate)) return false;
        if (endDate != null && now.after(endDate)) return false;
        
        return true;
    }
    
    @Override
    public String toString() {
        return roadName + " - " + closureType.getValue() + 
               (reason != null ? " (" + reason + ")" : "");
    }
}
