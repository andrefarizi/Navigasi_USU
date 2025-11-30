package com.mycompany.peta_usu.utils;

import javax.swing.JOptionPane;

/**
 * Utility untuk notifikasi refresh MapFrame setelah CRUD operations
 * Memberitahu user untuk refresh Google Maps setelah perubahan data
 */
public class MapRefreshUtil {
    
    /**
     * Notifikasi bahwa data telah berubah dan Maps perlu di-refresh
     */
    public static void notifyDataChanged(String entityName) {
        // User akan me-refresh MapFrame manual atau otomatis reload saat dibuka
        System.out.println("[MAP REFRESH] " + entityName + " data updated - Maps will auto-refresh on next load");
    }
    
    /**
     * Refresh markers di MapFrame setelah perubahan data
     */
    public static void refreshMarkers() {
        notifyDataChanged("Marker");
    }
    
    /**
     * Refresh buildings/locations di MapFrame
     */
    public static void refreshBuildings() {
        notifyDataChanged("Building");
    }
    
    /**
     * Refresh rooms
     */
    public static void refreshRooms() {
        notifyDataChanged("Room");
    }
    
    /**
     * Refresh roads
     */
    public static void refreshRoads() {
        notifyDataChanged("Road");
    }
    
    /**
     * Refresh facilities
     */
    public static void refreshFacilities() {
        notifyDataChanged("Facility");
    }
    
    /**
     * Show notification dengan opsi untuk buka Maps
     */
    public static void showMapRefreshNotification(String message) {
        int choice = JOptionPane.showConfirmDialog(
            null,
            message + "\n\nBuka Peta untuk melihat perubahan?",
            "Data Berhasil Diubah",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            try {
                // Open MapFrame in new window
                Class<?> mapFrameClass = Class.forName("com.mycompany.peta_usu.MapFrame");
                Object mapFrame = mapFrameClass.getDeclaredConstructor(String.class)
                    .newInstance("user");
                mapFrameClass.getMethod("setVisible", boolean.class).invoke(mapFrame, true);
            } catch (Exception e) {
                System.err.println("Could not open MapFrame: " + e.getMessage());
            }
        }
    }
}
