package com.mycompany.peta_usu.utils;

import com.mycompany.peta_usu.MapFrame;
import javax.swing.JOptionPane;
import java.lang.ref.WeakReference;

/**
 * Utility untuk notifikasi refresh MapFrame setelah CRUD operations
 * Memberitahu user untuk refresh Google Maps setelah perubahan data
 * 
 * === 4 PILAR OOP ===
 * 1. ENCAPSULATION: Field activeMapFrameRef PRIVATE STATIC
 * 2. INHERITANCE: None (utility class, tidak butuh inheritance)
 * 3. POLYMORPHISM: Method overloading notifyDataChanged() berbagai entity
 * 4. ABSTRACTION: Method notifyDataChanged() sembunyikan WeakReference logic
 * 
 * @author PETA_USU Team
 */
public class MapRefreshUtil {
    
    // ========== ENCAPSULATION: Field PRIVATE STATIC ==========
    // WeakReference untuk avoid memory leaks
    private static WeakReference<MapFrame> activeMapFrameRef = null;  // ← PRIVATE STATIC
    
    /**
     * Register active MapFrame for auto-refresh
     */
    public static void registerMapFrame(MapFrame mapFrame) {
        activeMapFrameRef = new WeakReference<>(mapFrame);
        System.out.println("[MAP REFRESH] MapFrame registered for auto-refresh");
    }
    
    /**
     * Unregister MapFrame
     */
    public static void unregisterMapFrame() {
        activeMapFrameRef = null;
        System.out.println("[MAP REFRESH] MapFrame unregistered");
    }
    
    /**
     * Notifikasi bahwa data telah berubah dan Maps perlu di-refresh
     * 
     * === ABSTRACTION: Sembunyikan WeakReference, null check, auto-refresh logic ===
     */
    public static void notifyDataChanged(String entityName) {
        // Auto-refresh active MapFrame if available
        if (activeMapFrameRef != null) {
            MapFrame mapFrame = activeMapFrameRef.get();
            if (mapFrame != null) {
                mapFrame.refreshLegendFromDatabase();
                System.out.println("[MAP REFRESH] " + entityName + " data updated - MapFrame auto-refreshed");
                return;
            }
        }
        // Fallback: just log
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
