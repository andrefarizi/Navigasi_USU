package com.mycompany.peta_usu.dao;

import com.mycompany.peta_usu.config.DatabaseConnection;
import com.mycompany.peta_usu.models.Facility;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO untuk tabel facilities
 */
public class FacilityDAO {
    private static final Logger logger = Logger.getLogger(FacilityDAO.class.getName());
    private final Connection connection;
    
    public FacilityDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    // Create
    public boolean insertFacility(Facility facility) {
        String sql = "INSERT INTO facilities (building_id, facility_name, facility_type, " +
                    "description, is_available) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, facility.getBuildingId());
            pstmt.setString(2, facility.getFacilityName());
            pstmt.setString(3, facility.getFacilityType().getValue());
            pstmt.setString(4, facility.getDescription());
            pstmt.setBoolean(5, facility.isAvailable());
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        facility.setFacilityId(rs.getInt(1));
                    }
                }
                logger.info("Facility inserted: " + facility.getFacilityName());
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting facility", e);
        }
        return false;
    }
    
    // Read All
    public List<Facility> getAllFacilities() {
        List<Facility> facilities = new ArrayList<>();
        String sql = "SELECT f.*, b.building_name FROM facilities f " +
                    "JOIN buildings b ON f.building_id = b.building_id " +
                    "ORDER BY b.building_name, f.facility_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                facilities.add(mapResultSetToFacility(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting all facilities", e);
        }
        return facilities;
    }
    
    // Read by ID
    public Facility getFacilityById(int facilityId) {
        String sql = "SELECT f.*, b.building_name FROM facilities f " +
                    "JOIN buildings b ON f.building_id = b.building_id " +
                    "WHERE f.facility_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, facilityId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToFacility(rs);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting facility by ID", e);
        }
        return null;
    }
    
    // Read by Building
    public List<Facility> getFacilitiesByBuilding(int buildingId) {
        List<Facility> facilities = new ArrayList<>();
        String sql = "SELECT f.*, b.building_name FROM facilities f " +
                    "JOIN buildings b ON f.building_id = b.building_id " +
                    "WHERE f.building_id = ? AND f.is_available = TRUE " +
                    "ORDER BY f.facility_type, f.facility_name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, buildingId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                facilities.add(mapResultSetToFacility(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting facilities by building", e);
        }
        return facilities;
    }
    
    // Update
    public boolean updateFacility(Facility facility) {
        String sql = "UPDATE facilities SET building_id = ?, facility_name = ?, " +
                    "facility_type = ?, description = ?, is_available = ? " +
                    "WHERE facility_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, facility.getBuildingId());
            pstmt.setString(2, facility.getFacilityName());
            pstmt.setString(3, facility.getFacilityType().getValue());
            pstmt.setString(4, facility.getDescription());
            pstmt.setBoolean(5, facility.isAvailable());
            pstmt.setInt(6, facility.getFacilityId());
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                logger.info("Facility updated: " + facility.getFacilityName());
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating facility", e);
        }
        return false;
    }
    
    // Delete
    public boolean deleteFacility(int facilityId) {
        String sql = "DELETE FROM facilities WHERE facility_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, facilityId);
            int affected = pstmt.executeUpdate();
            
            if (affected > 0) {
                logger.info("Facility deleted: " + facilityId);
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting facility", e);
        }
        return false;
    }
    
    // Helper
    private Facility mapResultSetToFacility(ResultSet rs) throws SQLException {
        Facility facility = new Facility();
        facility.setFacilityId(rs.getInt("facility_id"));
        facility.setBuildingId(rs.getInt("building_id"));
        facility.setFacilityName(rs.getString("facility_name"));
        facility.setFacilityType(Facility.FacilityType.fromString(rs.getString("facility_type")));
        facility.setDescription(rs.getString("description"));
        facility.setAvailable(rs.getBoolean("is_available"));
        facility.setCreatedAt(rs.getTimestamp("created_at"));
        facility.setUpdatedAt(rs.getTimestamp("updated_at"));
        facility.setBuildingName(rs.getString("building_name"));
        return facility;
    }
}
