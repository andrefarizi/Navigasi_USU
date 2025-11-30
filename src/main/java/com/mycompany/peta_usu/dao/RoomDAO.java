package com.mycompany.peta_usu.dao;

import com.mycompany.peta_usu.config.DatabaseConnection;
import com.mycompany.peta_usu.models.Room;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RoomDAO - Data Access Object untuk Room
 * Menangani operasi CRUD untuk ruangan/kelas
 * 
 * @author PETA_USU Team
 */
public class RoomDAO {
    
    private static final Logger logger = Logger.getLogger(RoomDAO.class.getName());
    private final Connection connection;
    
    public RoomDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    /**
     * Get rooms by building ID
     */
    public List<Room> getRoomsByBuilding(int buildingId) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.*, b.building_name " +
                    "FROM rooms r " +
                    "JOIN buildings b ON r.building_id = b.building_id " +
                    "WHERE r.building_id = ? AND r.is_available = TRUE " +
                    "ORDER BY r.floor_number, r.room_code";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, buildingId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapResultSetToRoom(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting rooms by building", e);
        }
        
        return rooms;
    }
    
    /**
     * Get room by ID
     */
    public Room getRoomById(int roomId) {
        String sql = "SELECT r.*, b.building_name " +
                    "FROM rooms r " +
                    "JOIN buildings b ON r.building_id = b.building_id " +
                    "WHERE r.room_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, roomId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRoom(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting room by ID", e);
        }
        
        return null;
    }
    
    /**
     * Get all rooms
     */
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.*, b.building_name " +
                    "FROM rooms r " +
                    "JOIN buildings b ON r.building_id = b.building_id " +
                    "ORDER BY b.building_name, r.floor_number, r.room_code";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting all rooms", e);
        }
        
        return rooms;
    }
    
    /**
     * Get room by code
     */
    public Room getRoomByCode(String roomCode) {
        String sql = "SELECT r.*, b.building_name " +
                    "FROM rooms r " +
                    "JOIN buildings b ON r.building_id = b.building_id " +
                    "WHERE r.room_code = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, roomCode);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRoom(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting room by code", e);
        }
        
        return null;
    }
    
    /**
     * Insert new room
     */
    public boolean insertRoom(Room room) {
        String sql = "INSERT INTO rooms (building_id, room_code, room_name, floor_number, " +
                    "room_type, capacity, description, is_available) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, room.getBuildingId());
            pstmt.setString(2, room.getRoomCode());
            pstmt.setString(3, room.getRoomName());
            pstmt.setInt(4, room.getFloorNumber());
            pstmt.setString(5, room.getRoomType().getValue());
            pstmt.setInt(6, room.getCapacity());
            pstmt.setString(7, room.getDescription());
            pstmt.setBoolean(8, room.isAvailable());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        room.setRoomId(generatedKeys.getInt(1));
                    }
                }
                logger.info("Room inserted successfully: " + room.getRoomCode());
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting room", e);
        }
        
        return false;
    }
    
    /**
     * Update room
     */
    public boolean updateRoom(Room room) {
        String sql = "UPDATE rooms SET building_id = ?, room_code = ?, room_name = ?, " +
                    "floor_number = ?, room_type = ?, capacity = ?, description = ?, " +
                    "is_available = ? WHERE room_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, room.getBuildingId());
            pstmt.setString(2, room.getRoomCode());
            pstmt.setString(3, room.getRoomName());
            pstmt.setInt(4, room.getFloorNumber());
            pstmt.setString(5, room.getRoomType().getValue());
            pstmt.setInt(6, room.getCapacity());
            pstmt.setString(7, room.getDescription());
            pstmt.setBoolean(8, room.isAvailable());
            pstmt.setInt(9, room.getRoomId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Room updated successfully: " + room.getRoomCode());
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating room", e);
        }
        
        return false;
    }
    
    /**
     * Delete room
     */
    public boolean deleteRoom(int roomId) {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, roomId);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Room deleted successfully: " + roomId);
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting room", e);
        }
        
        return false;
    }
    
    /**
     * Search rooms
     */
    public List<Room> searchRooms(String keyword) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.*, b.building_name " +
                    "FROM rooms r " +
                    "JOIN buildings b ON r.building_id = b.building_id " +
                    "WHERE (r.room_code LIKE ? OR r.room_name LIKE ?) " +
                    "AND r.is_available = TRUE " +
                    "ORDER BY r.room_code";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapResultSetToRoom(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching rooms", e);
        }
        
        return rooms;
    }
    
    /**
     * Map ResultSet to Room object
     */
    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setRoomId(rs.getInt("room_id"));
        room.setBuildingId(rs.getInt("building_id"));
        room.setRoomCode(rs.getString("room_code"));
        room.setRoomName(rs.getString("room_name"));
        room.setFloorNumber(rs.getInt("floor_number"));
        room.setRoomType(Room.RoomType.fromString(rs.getString("room_type")));
        room.setCapacity(rs.getInt("capacity"));
        room.setDescription(rs.getString("description"));
        room.setAvailable(rs.getBoolean("is_available"));
        room.setCreatedAt(rs.getTimestamp("created_at"));
        room.setUpdatedAt(rs.getTimestamp("updated_at"));
        
        // From join
        room.setBuildingName(rs.getString("building_name"));
        
        return room;
    }
}
