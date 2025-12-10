package com.mycompany.peta_usu.models;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * RoadClosure Model Class
 * Merepresentasikan penutupan jalan di area USU
 * 
 * === 4 PILAR OOP YANG DITERAPKAN ===
 * 
 * 1. ENCAPSULATION (Enkapsulasi):
 *    - Field closureId, roadId, dates PRIVATE
 *    - Method isCurrentlyActive() bungkus logic kompleks validasi tanggal
 *    - Tujuan: Sembunyikan detail validasi dari user
 * 
 * 2. INHERITANCE (Pewarisan):
 *    - Enum ClosureType mewarisi dari java.lang.Enum
 *    - Method fromString() untuk konversi String ke enum
 * 
 * 3. POLYMORPHISM (Polimorfisme):
 *    - Override toString() untuk format output custom
 *    - isCurrentlyActive() method dengan logic berbeda tergantung tipe closure
 * 
 * 4. ABSTRACTION (Abstraksi):
 *    - Abstraksi penutupan jalan dari database
 *    - Method isCurrentlyActive() sembunyikan kompleksitas cek tanggal/waktu
 *    - User cukup panggil method, tidak perlu tahu detail validasi
 * 
 * @author PETA_USU Team
 */
public class RoadClosure {
    
    // === ENCAPSULATION: Field PRIVATE ===
    
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
    
    // ========== ENCAPSULATION: Getters and Setters ==========
    // Controlled access pattern
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
     * 
     * === ABSTRACTION: Sembunyikan kompleksitas validasi tanggal ===
     * User cukup panggil isCurrentlyActive() tanpa tahu detail:
     * - Validasi isActive flag
     * - Compare dengan tanggal sekarang
     * - Check date range (startDate, endDate)
     */
    public boolean isCurrentlyActive() {
        if (!isActive) return false;
        
        java.util.Date now = new java.util.Date();
        
        // Check date range
        if (startDate != null && now.before(startDate)) return false;
        if (endDate != null && now.after(endDate)) return false;
        
        return true;
    }
    
    // === POLYMORPHISM: Override toString() ===
    @Override
    public String toString() {
        return roadName + " - " + closureType.getValue() + 
               (reason != null ? " (" + reason + ")" : "");
    }
}
