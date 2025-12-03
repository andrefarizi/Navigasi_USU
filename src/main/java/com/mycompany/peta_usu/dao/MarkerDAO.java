package com.mycompany.peta_usu.dao;

import com.mycompany.peta_usu.config.DatabaseConnection;
import com.mycompany.peta_usu.models.Marker;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MarkerDAO - Data Access Object untuk Marker
 * Menangani semua operasi CRUD untuk custom markers
 * 
 * @author PETA_USU Team
 */
public class MarkerDAO {
    
    private static final Logger logger = Logger.getLogger(MarkerDAO.class.getName());
    
    public MarkerDAO() {
    }
    
    /**
     * Get all active markers
     */
    public List<Marker> getAllMarkers() {
        List<Marker> markers = new ArrayList<>();
        String sql = "SELECT * FROM markers WHERE is_active = TRUE ORDER BY marker_name";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                markers.add(mapResultSetToMarker(rs));
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting all markers", e);
        }
        
        return markers;
    }
    
    /**
     * Get markers by type
     */
    public List<Marker> getMarkersByType(String markerType) {
        List<Marker> markers = new ArrayList<>();
        String sql = "SELECT * FROM markers WHERE marker_type = ? AND is_active = TRUE ORDER BY marker_name";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, markerType);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    markers.add(mapResultSetToMarker(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting markers by type", e);
        }
        
        return markers;
    }
    
    /**
     * Get marker by ID
     */
    public Marker getMarkerById(int markerId) {
        String sql = "SELECT * FROM markers WHERE marker_id = ?";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, markerId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMarker(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting marker by ID", e);
        }
        
        return null;
    }
    
    /**
     * Insert new marker
     */
    public boolean insertMarker(Marker marker) {
        String sql = "INSERT INTO markers (marker_name, marker_type, description, latitude, " +
                    "longitude, icon_path, icon_name, created_by, is_active) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, marker.getMarkerName());
            pstmt.setString(2, marker.getMarkerType());
            pstmt.setString(3, marker.getDescription());
            pstmt.setDouble(4, marker.getLatitude());
            pstmt.setDouble(5, marker.getLongitude());
            pstmt.setString(6, marker.getIconPath());
            pstmt.setString(7, marker.getIconName());
            pstmt.setInt(8, marker.getCreatedBy());
            pstmt.setBoolean(9, marker.isActive());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        marker.setMarkerId(generatedKeys.getInt(1));
                    }
                }
                logger.info("Marker inserted successfully: " + marker.getMarkerName());
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting marker", e);
        }
        
        return false;
    }
    
    /**
     * Update marker position (untuk drag-drop)
     */
    public boolean updateMarkerPosition(int markerId, double latitude, double longitude) {
        String sql = "UPDATE markers SET latitude = ?, longitude = ? WHERE marker_id = ?";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, latitude);
            pstmt.setDouble(2, longitude);
            pstmt.setInt(3, markerId);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Marker position updated: " + markerId);
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating marker position", e);
        }
        
        return false;
    }
    
    /**
     * Update marker
     */
    public boolean updateMarker(Marker marker) {
        String sql = "UPDATE markers SET marker_name = ?, marker_type = ?, description = ?, " +
                    "latitude = ?, longitude = ?, icon_path = ?, icon_name = ?, is_active = ? " +
                    "WHERE marker_id = ?";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, marker.getMarkerName());
            pstmt.setString(2, marker.getMarkerType());
            pstmt.setString(3, marker.getDescription());
            pstmt.setDouble(4, marker.getLatitude());
            pstmt.setDouble(5, marker.getLongitude());
            pstmt.setString(6, marker.getIconPath());
            pstmt.setString(7, marker.getIconName());
            pstmt.setBoolean(8, marker.isActive());
            pstmt.setInt(9, marker.getMarkerId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Marker updated successfully: " + marker.getMarkerName());
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating marker", e);
        }
        
        return false;
    }
    
    /**
     * Delete marker (soft delete)
     */
    public boolean deleteMarker(int markerId) {
        String sql = "UPDATE markers SET is_active = FALSE WHERE marker_id = ?";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, markerId);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Marker deleted successfully: " + markerId);
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting marker", e);
        }
        
        return false;
    }
    
    /**
     * Hard delete marker (permanent)
     */
    public boolean permanentDeleteMarker(int markerId) {
        String sql = "DELETE FROM markers WHERE marker_id = ?";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, markerId);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Marker permanently deleted: " + markerId);
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error permanently deleting marker", e);
        }
        
        return false;
    }
    
    /**
     * Map ResultSet to Marker object
     */
    private Marker mapResultSetToMarker(ResultSet rs) throws SQLException {
        Marker marker = new Marker();
        marker.setMarkerId(rs.getInt("marker_id"));
        marker.setMarkerName(rs.getString("marker_name"));
        marker.setMarkerType(rs.getString("marker_type"));
        marker.setDescription(rs.getString("description"));
        marker.setLatitude(rs.getDouble("latitude"));
        marker.setLongitude(rs.getDouble("longitude"));
        marker.setIconPath(rs.getString("icon_path"));
        marker.setIconName(rs.getString("icon_name"));
        marker.setCreatedBy(rs.getInt("created_by"));
        marker.setActive(rs.getBoolean("is_active"));
        marker.setCreatedAt(rs.getTimestamp("created_at"));
        marker.setUpdatedAt(rs.getTimestamp("updated_at"));
        return marker;
    }
}
