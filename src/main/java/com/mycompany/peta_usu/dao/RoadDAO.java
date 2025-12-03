package com.mycompany.peta_usu.dao;

import com.mycompany.peta_usu.config.DatabaseConnection;
import com.mycompany.peta_usu.models.Road;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO untuk tabel roads
 */
public class RoadDAO {
    private static final Logger logger = Logger.getLogger(RoadDAO.class.getName());
    
    public RoadDAO() {
    }
    
    // Create
    public boolean insertRoad(Road road) {
        String sql = "INSERT INTO roads (road_name, road_type, start_lat, start_lng, " +
                    "end_lat, end_lng, is_one_way, direction, is_active, distance, description, " +
                    "polyline_points, google_road_name, road_segments, last_gmaps_update) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, road.getRoadName());
            pstmt.setString(2, road.getRoadType() != null ? road.getRoadType().getValue() : Road.RoadType.NORMAL.getValue());
            pstmt.setDouble(3, road.getStartLat());
            pstmt.setDouble(4, road.getStartLng());
            pstmt.setDouble(5, road.getEndLat());
            pstmt.setDouble(6, road.getEndLng());
            pstmt.setBoolean(7, road.isOneWay());
            pstmt.setString(8, "normal"); // direction - default to normal
            pstmt.setBoolean(9, true); // is_active - default to true
            pstmt.setDouble(10, road.getDistance());
            pstmt.setString(11, road.getDescription());
            pstmt.setString(12, road.getPolylinePoints());
            pstmt.setString(13, road.getGoogleRoadName());
            pstmt.setString(14, road.getRoadSegments());
            pstmt.setTimestamp(15, road.getLastGmapsUpdate());
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        road.setRoadId(rs.getInt(1));
                    }
                }
                logger.info("Road inserted: " + road.getRoadName());
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting road", e);
        }
        return false;
    }
    
    // Read All
    public List<Road> getAllRoads() {
        List<Road> roads = new ArrayList<>();
        String sql = "SELECT * FROM roads ORDER BY road_name";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                roads.add(mapResultSetToRoad(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting all roads", e);
        }
        return roads;
    }
    
    // Read by ID
    public Road getRoadById(int roadId) {
        String sql = "SELECT * FROM roads WHERE road_id = ?";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, roadId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToRoad(rs);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting road by ID", e);
        }
        return null;
    }
    
    // Update
    public boolean updateRoad(Road road) {
        String sql = "UPDATE roads SET road_name = ?, road_type = ?, start_lat = ?, " +
                    "start_lng = ?, end_lat = ?, end_lng = ?, is_one_way = ?, direction = ?, is_active = ?, " +
                    "distance = ?, description = ?, polyline_points = ?, google_road_name = ?, " +
                    "road_segments = ?, last_gmaps_update = ? WHERE road_id = ?";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, road.getRoadName());
            pstmt.setString(2, road.getRoadType() != null ? road.getRoadType().getValue() : Road.RoadType.NORMAL.getValue());
            pstmt.setDouble(3, road.getStartLat());
            pstmt.setDouble(4, road.getStartLng());
            pstmt.setDouble(5, road.getEndLat());
            pstmt.setDouble(6, road.getEndLng());
            pstmt.setBoolean(7, road.isOneWay());
            pstmt.setString(8, "normal"); // direction
            pstmt.setBoolean(9, true); // is_active
            pstmt.setDouble(10, road.getDistance());
            pstmt.setString(11, road.getDescription());
            pstmt.setString(12, road.getPolylinePoints());
            pstmt.setString(13, road.getGoogleRoadName());
            pstmt.setString(14, road.getRoadSegments());
            pstmt.setTimestamp(15, road.getLastGmapsUpdate());
            pstmt.setInt(16, road.getRoadId());
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                logger.info("Road updated: " + road.getRoadName());
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating road", e);
        }
        return false;
    }
    
    // Delete
    public boolean deleteRoad(int roadId) {
        String sql = "DELETE FROM roads WHERE road_id = ?";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, roadId);
            int affected = pstmt.executeUpdate();
            
            if (affected > 0) {
                logger.info("Road deleted: " + roadId);
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting road", e);
        }
        return false;
    }
    
    // Search
    public List<Road> searchRoads(String keyword) {
        List<Road> roads = new ArrayList<>();
        String sql = "SELECT * FROM roads WHERE road_name LIKE ? OR description LIKE ? " +
                    "ORDER BY road_name";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                roads.add(mapResultSetToRoad(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching roads", e);
        }
        return roads;
    }
    
    // Helper
    private Road mapResultSetToRoad(ResultSet rs) throws SQLException {
        Road road = new Road();
        road.setRoadId(rs.getInt("road_id"));
        road.setRoadName(rs.getString("road_name"));
        road.setRoadType(Road.RoadType.fromString(rs.getString("road_type")));
        road.setStartLat(rs.getDouble("start_lat"));
        road.setStartLng(rs.getDouble("start_lng"));
        road.setEndLat(rs.getDouble("end_lat"));
        road.setEndLng(rs.getDouble("end_lng"));
        road.setOneWay(rs.getBoolean("is_one_way"));
        
        // Try to get optional fields (may not exist in old schema)
        try {
            road.setDistance(rs.getDouble("distance"));
        } catch (SQLException e) { /* ignore if column doesn't exist */ }
        
        try {
            road.setDescription(rs.getString("description"));
        } catch (SQLException e) { /* ignore if column doesn't exist */ }
        
        try {
            road.setPolylinePoints(rs.getString("polyline_points"));
        } catch (SQLException e) { /* ignore if column doesn't exist */ }
        
        try {
            road.setGoogleRoadName(rs.getString("google_road_name"));
        } catch (SQLException e) { /* ignore if column doesn't exist */ }
        
        try {
            road.setRoadSegments(rs.getString("road_segments"));
        } catch (SQLException e) { /* ignore if column doesn't exist */ }
        
        try {
            road.setLastGmapsUpdate(rs.getTimestamp("last_gmaps_update"));
        } catch (SQLException e) { /* ignore if column doesn't exist */ }
        
        road.setCreatedAt(rs.getTimestamp("created_at"));
        road.setUpdatedAt(rs.getTimestamp("updated_at"));
        return road;
    }
}
