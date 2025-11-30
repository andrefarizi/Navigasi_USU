package com.mycompany.peta_usu.utils;

import com.mycompany.peta_usu.config.DatabaseConnection;
import com.mycompany.peta_usu.dao.*;
import com.mycompany.peta_usu.models.*;
import java.sql.*;
import java.util.List;

/**
 * DatabaseDebugger - Utility untuk debugging dan verifikasi database
 * Gunakan untuk memeriksa status koneksi dan data
 */
public class DatabaseDebugger {
    
    public static void main(String[] args) {
        System.out.println("=== PETA USU Database Debugger ===\n");
        
        // 1. Test koneksi database
        testConnection();
        
        // 2. Cek data di setiap tabel
        checkRoadsData();
        checkRoadClosuresData();
        checkMarkersData();
        checkBuildingsData();
        checkFacilitiesData();
        checkRoomsData();
        checkUsersData();
        
        System.out.println("\n=== Debugging Complete ===");
    }
    
    private static void testConnection() {
        System.out.println("1. Testing Database Connection...");
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                DatabaseMetaData metadata = conn.getMetaData();
                System.out.println("   ✓ Connection OK");
                System.out.println("   Database: " + metadata.getDatabaseProductName());
                System.out.println("   Version: " + metadata.getDatabaseProductVersion());
                System.out.println("   URL: " + metadata.getURL());
            } else {
                System.out.println("   ✗ Connection FAILED - Connection is null or closed");
            }
        } catch (SQLException e) {
            System.out.println("   ✗ Connection FAILED: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private static void checkRoadsData() {
        System.out.println("2. Checking ROADS table...");
        try {
            RoadDAO roadDAO = new RoadDAO();
            List<Road> roads = roadDAO.getAllRoads();
            
            if (roads == null) {
                System.out.println("   ✗ Roads list is NULL");
            } else if (roads.isEmpty()) {
                System.out.println("   ⚠ No roads found in database");
                System.out.println("   → Please run SQL script to insert sample road data");
            } else {
                System.out.println("   ✓ Found " + roads.size() + " roads");
                System.out.println("   Sample roads:");
                for (int i = 0; i < Math.min(3, roads.size()); i++) {
                    Road road = roads.get(i);
                    System.out.println("     - " + road.getRoadName() + 
                        " (" + road.getRoadType() + ", " + road.getDistance() + "m)");
                }
            }
        } catch (Exception e) {
            System.out.println("   ✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private static void checkRoadClosuresData() {
        System.out.println("3. Checking ROAD_CLOSURES table...");
        try {
            RoadClosureDAO closureDAO = new RoadClosureDAO();
            List<RoadClosure> closures = closureDAO.getActiveClosures();
            
            if (closures == null) {
                System.out.println("   ✗ Closures list is NULL");
            } else if (closures.isEmpty()) {
                System.out.println("   ✓ No active road closures (this is normal)");
            } else {
                System.out.println("   ✓ Found " + closures.size() + " active closures");
                for (RoadClosure closure : closures) {
                    System.out.println("     - Road ID " + closure.getRoadId() + 
                        " (" + closure.getClosureType() + ")");
                }
            }
        } catch (Exception e) {
            System.out.println("   ✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private static void checkMarkersData() {
        System.out.println("4. Checking MARKERS table...");
        try {
            MarkerDAO markerDAO = new MarkerDAO();
            List<Marker> markers = markerDAO.getAllMarkers();
            
            if (markers == null) {
                System.out.println("   ✗ Markers list is NULL");
            } else if (markers.isEmpty()) {
                System.out.println("   ⚠ No markers found");
            } else {
                System.out.println("   ✓ Found " + markers.size() + " markers");
                System.out.println("   Sample markers:");
                for (int i = 0; i < Math.min(3, markers.size()); i++) {
                    Marker marker = markers.get(i);
                    System.out.println("     - " + marker.getMarkerName() + 
                        " (" + marker.getMarkerType() + ")");
                }
            }
        } catch (Exception e) {
            System.out.println("   ✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private static void checkBuildingsData() {
        System.out.println("5. Checking BUILDINGS table...");
        try {
            BuildingDAO buildingDAO = new BuildingDAO();
            List<Building> buildings = buildingDAO.getAllBuildings();
            
            if (buildings == null) {
                System.out.println("   ✗ Buildings list is NULL");
            } else if (buildings.isEmpty()) {
                System.out.println("   ⚠ No buildings found");
            } else {
                System.out.println("   ✓ Found " + buildings.size() + " buildings");
                System.out.println("   Sample buildings:");
                for (int i = 0; i < Math.min(3, buildings.size()); i++) {
                    Building building = buildings.get(i);
                    System.out.println("     - " + building.getBuildingName() + 
                        " (" + building.getBuildingType() + ")");
                }
            }
        } catch (Exception e) {
            System.out.println("   ✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private static void checkFacilitiesData() {
        System.out.println("6. Checking FACILITIES table...");
        try {
            FacilityDAO facilityDAO = new FacilityDAO();
            List<Facility> facilities = facilityDAO.getAllFacilities();
            
            if (facilities == null) {
                System.out.println("   ✗ Facilities list is NULL");
            } else if (facilities.isEmpty()) {
                System.out.println("   ✓ No facilities (this is normal)");
            } else {
                System.out.println("   ✓ Found " + facilities.size() + " facilities");
            }
        } catch (Exception e) {
            System.out.println("   ✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private static void checkRoomsData() {
        System.out.println("7. Checking ROOMS table...");
        try {
            RoomDAO roomDAO = new RoomDAO();
            List<Room> rooms = roomDAO.getAllRooms();
            
            if (rooms == null) {
                System.out.println("   ✗ Rooms list is NULL");
            } else if (rooms.isEmpty()) {
                System.out.println("   ✓ No rooms (this is normal)");
            } else {
                System.out.println("   ✓ Found " + rooms.size() + " rooms");
            }
        } catch (Exception e) {
            System.out.println("   ✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    private static void checkUsersData() {
        System.out.println("8. Checking USERS table...");
        try {
            UserDAO userDAO = new UserDAO();
            List<User> users = userDAO.getAllUsers();
            
            if (users == null) {
                System.out.println("   ✗ Users list is NULL");
            } else if (users.isEmpty()) {
                System.out.println("   ⚠ No users found");
            } else {
                System.out.println("   ✓ Found " + users.size() + " users");
            }
        } catch (Exception e) {
            System.out.println("   ✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
}
