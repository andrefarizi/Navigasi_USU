package com.mycompany.peta_usu.dao;

import com.mycompany.peta_usu.config.DatabaseConnection;
import com.mycompany.peta_usu.models.RoadClosure;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RoadClosureDAO - Data Access Object untuk RoadClosure
 * Menangani operasi penutupan jalan
 * 
 * @author PETA_USU Team
 */
public class RoadClosureDAO {
    
    private static final Logger logger = Logger.getLogger(RoadClosureDAO.class.getName());
    private final Connection connection;
    
    public RoadClosureDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    /**
     * Get all active road closures
     */
    public List<RoadClosure> getActiveClosures() {
        List<RoadClosure> closures = new ArrayList<>();
        String sql = "SELECT rc.*, r.road_name, u.name as created_by_name " +
                    "FROM road_closures rc " +
                    "JOIN roads r ON rc.road_id = r.road_id " +
                    "LEFT JOIN users u ON rc.created_by = u.user_id " +
                    "WHERE rc.is_active = TRUE " +
                    "AND (rc.end_date IS NULL OR rc.end_date >= CURDATE()) " +
                    "ORDER BY rc.created_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                closures.add(mapResultSetToRoadClosure(rs));
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting active closures", e);
        }
        
        return closures;
    }
    
    /**
     * Get closure by ID
     */
    public RoadClosure getClosureById(int closureId) {
        String sql = "SELECT rc.*, r.road_name, u.name as created_by_name " +
                    "FROM road_closures rc " +
                    "JOIN roads r ON rc.road_id = r.road_id " +
                    "LEFT JOIN users u ON rc.created_by = u.user_id " +
                    "WHERE rc.closure_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, closureId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRoadClosure(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting closure by ID", e);
        }
        
        return null;
    }
    
    /**
     * Insert new road closure
     */
    public boolean insertClosure(RoadClosure closure) {
        String sql = "INSERT INTO road_closures (road_id, closure_type, reason, start_date, " +
                    "end_date, start_time, end_time, is_active, created_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, closure.getRoadId());
            pstmt.setString(2, closure.getClosureType().getValue());
            pstmt.setString(3, closure.getReason());
            pstmt.setDate(4, closure.getStartDate());
            pstmt.setDate(5, closure.getEndDate());
            pstmt.setTime(6, closure.getStartTime());
            pstmt.setTime(7, closure.getEndTime());
            pstmt.setBoolean(8, closure.isActive());
            pstmt.setInt(9, closure.getCreatedBy());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        closure.setClosureId(generatedKeys.getInt(1));
                    }
                }
                logger.info("Road closure inserted successfully");
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting road closure", e);
        }
        
        return false;
    }
    
    /**
     * Update road closure
     */
    public boolean updateClosure(RoadClosure closure) {
        String sql = "UPDATE road_closures SET road_id = ?, closure_type = ?, reason = ?, " +
                    "start_date = ?, end_date = ?, start_time = ?, end_time = ?, is_active = ? " +
                    "WHERE closure_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, closure.getRoadId());
            pstmt.setString(2, closure.getClosureType().getValue());
            pstmt.setString(3, closure.getReason());
            pstmt.setDate(4, closure.getStartDate());
            pstmt.setDate(5, closure.getEndDate());
            pstmt.setTime(6, closure.getStartTime());
            pstmt.setTime(7, closure.getEndTime());
            pstmt.setBoolean(8, closure.isActive());
            pstmt.setInt(9, closure.getClosureId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Road closure updated successfully");
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating road closure", e);
        }
        
        return false;
    }
    
    /**
     * Delete closure (soft delete)
     */
    public boolean deleteClosure(int closureId) {
        String sql = "UPDATE road_closures SET is_active = FALSE WHERE closure_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, closureId);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Road closure deleted successfully: " + closureId);
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting road closure", e);
        }
        
        return false;
    }
    
    /**
     * Get closures by type
     */
    public List<RoadClosure> getClosuresByType(RoadClosure.ClosureType type) {
        List<RoadClosure> closures = new ArrayList<>();
        String sql = "SELECT rc.*, r.road_name, u.name as created_by_name " +
                    "FROM road_closures rc " +
                    "JOIN roads r ON rc.road_id = r.road_id " +
                    "LEFT JOIN users u ON rc.created_by = u.user_id " +
                    "WHERE rc.closure_type = ? AND rc.is_active = TRUE";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, type.getValue());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    closures.add(mapResultSetToRoadClosure(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting closures by type", e);
        }
        
        return closures;
    }
    
    /**
     * Map ResultSet to RoadClosure object
     */
    private RoadClosure mapResultSetToRoadClosure(ResultSet rs) throws SQLException {
        RoadClosure closure = new RoadClosure();
        closure.setClosureId(rs.getInt("closure_id"));
        closure.setRoadId(rs.getInt("road_id"));
        closure.setClosureType(RoadClosure.ClosureType.fromString(rs.getString("closure_type")));
        closure.setReason(rs.getString("reason"));
        closure.setStartDate(rs.getDate("start_date"));
        closure.setEndDate(rs.getDate("end_date"));
        closure.setStartTime(rs.getTime("start_time"));
        closure.setEndTime(rs.getTime("end_time"));
        closure.setActive(rs.getBoolean("is_active"));
        closure.setCreatedBy(rs.getInt("created_by"));
        closure.setCreatedAt(rs.getTimestamp("created_at"));
        closure.setUpdatedAt(rs.getTimestamp("updated_at"));
        
        // Additional fields from join
        closure.setRoadName(rs.getString("road_name"));
        closure.setCreatedByName(rs.getString("created_by_name"));
        
        return closure;
    }
}
