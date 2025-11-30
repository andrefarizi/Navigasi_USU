package com.mycompany.peta_usu.dao;

import com.mycompany.peta_usu.config.DatabaseConnection;
import com.mycompany.peta_usu.models.Building;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BuildingDAO - Data Access Object untuk Building
 * Menangani semua operasi CRUD untuk building
 * 
 * @author PETA_USU Team
 */
public class BuildingDAO {
    
    private static final Logger logger = Logger.getLogger(BuildingDAO.class.getName());
    private final Connection connection;
    
    public BuildingDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    /**
     * Get all buildings
     */
    public List<Building> getAllBuildings() {
        List<Building> buildings = new ArrayList<>();
        String sql = "SELECT * FROM buildings WHERE is_active = TRUE ORDER BY building_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                buildings.add(mapResultSetToBuilding(rs));
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting all buildings", e);
        }
        
        return buildings;
    }
    
    /**
     * Get buildings by type
     */
    public List<Building> getBuildingsByType(Building.BuildingType type) {
        List<Building> buildings = new ArrayList<>();
        String sql = "SELECT * FROM buildings WHERE building_type = ? AND is_active = TRUE ORDER BY building_name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, type.getValue());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    buildings.add(mapResultSetToBuilding(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting buildings by type", e);
        }
        
        return buildings;
    }
    
    /**
     * Get building by ID
     */
    public Building getBuildingById(int buildingId) {
        String sql = "SELECT * FROM buildings WHERE building_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, buildingId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBuilding(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting building by ID", e);
        }
        
        return null;
    }
    
    /**
     * Get building by code
     */
    public Building getBuildingByCode(String buildingCode) {
        String sql = "SELECT * FROM buildings WHERE building_code = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, buildingCode);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBuilding(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting building by code", e);
        }
        
        return null;
    }
    
    /**
     * Insert new building
     */
    public boolean insertBuilding(Building building) {
        String sql = "INSERT INTO buildings (building_code, building_name, building_type, " +
                    "description, latitude, longitude, address, floor_count, icon_path, is_active) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, building.getBuildingCode());
            pstmt.setString(2, building.getBuildingName());
            pstmt.setString(3, building.getBuildingType().getValue());
            pstmt.setString(4, building.getDescription());
            pstmt.setDouble(5, building.getLatitude());
            pstmt.setDouble(6, building.getLongitude());
            pstmt.setString(7, building.getAddress());
            pstmt.setInt(8, building.getFloorCount());
            pstmt.setString(9, building.getIconPath());
            pstmt.setBoolean(10, building.isActive());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        building.setBuildingId(generatedKeys.getInt(1));
                    }
                }
                logger.info("Building inserted successfully: " + building.getBuildingName());
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting building", e);
        }
        
        return false;
    }
    
    /**
     * Update building
     */
    public boolean updateBuilding(Building building) {
        String sql = "UPDATE buildings SET building_code = ?, building_name = ?, building_type = ?, " +
                    "description = ?, latitude = ?, longitude = ?, address = ?, floor_count = ?, " +
                    "icon_path = ?, is_active = ? WHERE building_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, building.getBuildingCode());
            pstmt.setString(2, building.getBuildingName());
            pstmt.setString(3, building.getBuildingType().getValue());
            pstmt.setString(4, building.getDescription());
            pstmt.setDouble(5, building.getLatitude());
            pstmt.setDouble(6, building.getLongitude());
            pstmt.setString(7, building.getAddress());
            pstmt.setInt(8, building.getFloorCount());
            pstmt.setString(9, building.getIconPath());
            pstmt.setBoolean(10, building.isActive());
            pstmt.setInt(11, building.getBuildingId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Building updated successfully: " + building.getBuildingName());
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating building", e);
        }
        
        return false;
    }
    
    /**
     * Delete building (soft delete)
     */
    public boolean deleteBuilding(int buildingId) {
        String sql = "UPDATE buildings SET is_active = FALSE WHERE building_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, buildingId);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Building deleted successfully: " + buildingId);
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting building", e);
        }
        
        return false;
    }
    
    /**
     * Search buildings by name
     */
    public List<Building> searchBuildings(String keyword) {
        List<Building> buildings = new ArrayList<>();
        String sql = "SELECT * FROM buildings WHERE (building_name LIKE ? OR building_code LIKE ?) " +
                    "AND is_active = TRUE ORDER BY building_name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    buildings.add(mapResultSetToBuilding(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching buildings", e);
        }
        
        return buildings;
    }
    
    /**
     * Get buildings within radius (km)
     */
    public List<Building> getBuildingsNearby(double latitude, double longitude, double radiusKm) {
        List<Building> allBuildings = getAllBuildings();
        List<Building> nearbyBuildings = new ArrayList<>();
        
        for (Building building : allBuildings) {
            double distance = building.distanceFrom(latitude, longitude);
            if (distance <= radiusKm * 1000) { // convert to meters
                nearbyBuildings.add(building);
            }
        }
        
        return nearbyBuildings;
    }
    
    /**
     * Map ResultSet to Building object
     */
    private Building mapResultSetToBuilding(ResultSet rs) throws SQLException {
        Building building = new Building();
        building.setBuildingId(rs.getInt("building_id"));
        building.setBuildingCode(rs.getString("building_code"));
        building.setBuildingName(rs.getString("building_name"));
        building.setBuildingType(Building.BuildingType.fromString(rs.getString("building_type")));
        building.setDescription(rs.getString("description"));
        building.setLatitude(rs.getDouble("latitude"));
        building.setLongitude(rs.getDouble("longitude"));
        building.setAddress(rs.getString("address"));
        building.setFloorCount(rs.getInt("floor_count"));
        building.setIconPath(rs.getString("icon_path"));
        building.setActive(rs.getBoolean("is_active"));
        building.setCreatedAt(rs.getTimestamp("created_at"));
        building.setUpdatedAt(rs.getTimestamp("updated_at"));
        return building;
    }
}
