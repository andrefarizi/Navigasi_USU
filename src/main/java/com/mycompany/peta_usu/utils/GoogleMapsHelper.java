package com.mycompany.peta_usu.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GoogleMapsHelper - Utility untuk Google Maps API
 * Helper class untuk integrasi Google Maps
 * 
 * === 4 PILAR OOP YANG DITERAPKAN ===
 * 
 * 1. ENCAPSULATION (Enkapsulasi):
 *    - API_KEY disimpan sebagai PRIVATE STATIC FINAL (constant)
 *    - Tidak bisa diubah atau diakses langsung dari luar
 *    - Tujuan: Keamanan API key, cegah penyalahgunaan
 * 
 * 2. INHERITANCE (Pewarisan):
 *    - Inner class MapMarker bisa di-extend untuk custom marker
 *    - Method overloading: generateMapHTMLWithMarkers() punya 2 versi
 *    - Versi 1: Tanpa parameter center (pakai default USU_CENTER)
 *    - Versi 2: Dengan parameter center yang custom
 * 
 * 3. POLYMORPHISM (Polimorfisme):
 *    - Method overloading: generateMapHTMLWithMarkers(markers) vs generateMapHTMLWithMarkers(lat, lng, zoom, markers)
 *    - Constructor overloading di MapMarker: 3 versi constructor berbeda
 *    - Tujuan: Fleksibilitas pemanggilan method sesuai kebutuhan
 * 
 * 4. ABSTRACTION (Abstraksi):
 *    - Class ini ABSTRAKSI dari Google Maps API yang kompleks
 *    - User tidak perlu tahu: HTTP request, URL encoding, JSON parsing
 *    - Cukup panggil: getStaticMapUrl() atau generateMapHTML()
 *    - Hasil: URL atau HTML siap pakai untuk tampilkan peta
 *    - Tujuan: Sederhanakan integrasi Google Maps
 * 
 * @author PETA_USU Team
 */
public class GoogleMapsHelper {
    
    private static final Logger logger = Logger.getLogger(GoogleMapsHelper.class.getName());
    private static final String API_KEY = "AIzaSyBy-ugy58EBTMwG2TqtBVlPhR8oF3LeMhA";
    
    // USU Coordinates (center of campus)
    public static final double USU_CENTER_LAT = 3.5690;
    public static final double USU_CENTER_LNG = 98.6560;
    
    /**
     * Generate Google Maps static image URL
     */
    public static String getStaticMapUrl(double latitude, double longitude, int width, int height, int zoom) {
        try {
            String url = String.format(
                "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=%d&size=%dx%d&maptype=roadmap&key=%s",
                latitude, longitude, zoom, width, height, API_KEY
            );
            return url;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error generating static map URL", e);
            return null;
        }
    }
    
    /**
     * Generate Google Maps static image URL with marker
     */
    public static String getStaticMapUrlWithMarker(double latitude, double longitude, 
                                                    int width, int height, int zoom, String markerLabel) {
        try {
            String url = String.format(
                "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=%d&size=%dx%d&markers=color:red%%7Clabel:%s%%7C%f,%f&maptype=roadmap&key=%s",
                latitude, longitude, zoom, width, height, 
                URLEncoder.encode(markerLabel, "UTF-8"), 
                latitude, longitude, API_KEY
            );
            return url;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error generating static map URL with marker", e);
            return null;
        }
    }
    
    /**
     * Generate Google Maps embed URL untuk iframe/WebView
     */
    public static String getEmbedMapUrl(double latitude, double longitude, int zoom) {
        return String.format(
            "https://www.google.com/maps/embed/v1/view?key=%s&center=%f,%f&zoom=%d",
            API_KEY, latitude, longitude, zoom
        );
    }
    
    /**
     * Generate Google Maps interactive HTML
     */
    public static String generateMapHTML(double latitude, double longitude, int zoom, String title) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='utf-8'>");
        html.append("<title>").append(title).append("</title>");
        html.append("<script src='https://maps.googleapis.com/maps/api/js?key=").append(API_KEY).append("'></script>");
        html.append("<style>");
        html.append("body, html { margin: 0; padding: 0; height: 100%; }");
        html.append("#map { height: 100%; width: 100%; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div id='map'></div>");
        html.append("<script>");
        html.append("function initMap() {");
        html.append("  var location = {lat: ").append(latitude).append(", lng: ").append(longitude).append("};");
        html.append("  var map = new google.maps.Map(document.getElementById('map'), {");
        html.append("    zoom: ").append(zoom).append(",");
        html.append("    center: location,");
        html.append("    mapTypeId: 'roadmap'");
        html.append("  });");
        html.append("  var marker = new google.maps.Marker({");
        html.append("    position: location,");
        html.append("    map: map,");
        html.append("    title: '").append(title).append("'");
        html.append("  });");
        html.append("}");
        html.append("initMap();");
        html.append("</script>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
    
    /**
     * Generate HTML with multiple markers - overloaded version with default center
     * 
     * === POLYMORPHISM: Method Overloading ===
     * Method ini OVERLOAD dari generateMapHTMLWithMarkers() yang lain
     * Punya nama sama tapi parameter BERBEDA
     * Versi 1 (ini): Cuma butuh List<MapMarker>
     * Versi 2 (bawah): Butuh centerLat, centerLng, zoom, markers
     * Java otomatis pilih method yang sesuai berdasarkan parameter
     */
    public static String generateMapHTMLWithMarkers(java.util.List<MapMarker> markers) {
        return generateMapHTMLWithMarkers(USU_CENTER_LAT, USU_CENTER_LNG, 15, markers);
    }
    
    /**
     * Generate HTML with multiple markers
     * === POLYMORPHISM: Method Overloading (versi lengkap) ===
     */
    public static String generateMapHTMLWithMarkers(double centerLat, double centerLng, 
                                                     int zoom, java.util.List<MapMarker> markers) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='utf-8'>");
        html.append("<title>Peta USU</title>");
        html.append("<script src='https://maps.googleapis.com/maps/api/js?key=").append(API_KEY).append("'></script>");
        html.append("<style>");
        html.append("body, html { margin: 0; padding: 0; height: 100%; }");
        html.append("#map { height: 100%; width: 100%; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div id='map'></div>");
        html.append("<script>");
        html.append("function initMap() {");
        html.append("  var center = {lat: ").append(centerLat).append(", lng: ").append(centerLng).append("};");
        html.append("  var map = new google.maps.Map(document.getElementById('map'), {");
        html.append("    zoom: ").append(zoom).append(",");
        html.append("    center: center,");
        html.append("    mapTypeId: 'roadmap'");
        html.append("  });");
        
        // Add markers
        if (markers != null && !markers.isEmpty()) {
            for (MapMarker marker : markers) {
                html.append("  var marker").append(marker.id).append(" = new google.maps.Marker({");
                html.append("    position: {lat: ").append(marker.lat).append(", lng: ").append(marker.lng).append("},");
                html.append("    map: map,");
                html.append("    title: '").append(marker.title).append("',");
                if (marker.icon != null) {
                    html.append("    icon: '").append(marker.icon).append("',");
                }
                html.append("    draggable: ").append(marker.draggable);
                html.append("  });");
                
                // Add click event
                html.append("  marker").append(marker.id).append(".addListener('click', function() {");
                html.append("    var infoWindow = new google.maps.InfoWindow({");
                html.append("      content: '<h3>").append(marker.title).append("</h3><p>").append(marker.description).append("</p>'");
                html.append("    });");
                html.append("    infoWindow.open(map, marker").append(marker.id).append(");");
                html.append("  });");
                
                // Add drag event if draggable
                if (marker.draggable) {
                    html.append("  marker").append(marker.id).append(".addListener('dragend', function(event) {");
                    html.append("    console.log('Marker dropped at: ' + event.latLng.lat() + ', ' + event.latLng.lng());");
                    html.append("  });");
                }
            }
        }
        
        html.append("}");
        html.append("initMap();");
        html.append("</script>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
    
    /**
     * Get directions between two points
     */
    public static String getDirections(double startLat, double startLng, double endLat, double endLng) {
        try {
            String urlString = String.format(
                "https://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&key=%s",
                startLat, startLng, endLat, endLng, API_KEY
            );
            
            URI uri = new URI(urlString);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            return response.toString();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting directions", e);
            return null;
        }
    }
    
    /**
     * Helper class untuk marker data
     */
    public static class MapMarker {
        public int id;
        public double lat;
        public double lng;
        public String title;
        public String description;
        public String icon;
        public boolean draggable;
        
        // === POLYMORPHISM: Constructor Overloading ===
        // 3 constructor berbeda untuk fleksibilitas pembuatan MapMarker
        
        // Constructor 1: Simple (untuk user map, id auto 0)
        public MapMarker(double lat, double lng, String title, String description) {
            this(0, lat, lng, title, description);
        }
        
        // Constructor 2: Dengan ID (untuk admin map)
        public MapMarker(int id, double lat, double lng, String title, String description) {
            this.id = id;
            this.lat = lat;
            this.lng = lng;
            this.title = title;
            this.description = description;
            this.draggable = false;
        }
        
        public MapMarker(int id, double lat, double lng, String title, String description, boolean draggable) {
            this(id, lat, lng, title, description);
            this.draggable = draggable;
        }
    }
    
    /**
     * Calculate distance between two coordinates (Haversine formula)
     */
    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distance in km
    }
}
