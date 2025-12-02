package com.mycompany.peta_usu.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jxmapviewer.viewer.GeoPosition;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GoogleMapsRoadService - Service untuk mendapatkan informasi jalan dari Google Maps API
 * 
 * Features:
 * - Fetch road names (e.g., "Jl. Alumni", "Jl. Perpustakaan")
 * - Get polyline yang mengikuti jalan sebenarnya (bukan garis lurus)
 * - Snap coordinates to nearest roads
 * 
 * @author PETA_USU Team
 */
public class GoogleMapsRoadService {
    
    private static final Logger logger = Logger.getLogger(GoogleMapsRoadService.class.getName());
    private static final String API_KEY = "AIzaSyBy-ugy58EBTMwG2TqtBVlPhR8oF3LeMhA";
    
    /**
     * Road information dari Google Maps
     */
    public static class RoadInfo {
        public String roadName;           // e.g., "Jl. Alumni"
        public String fullName;           // e.g., "Jalan Alumni, Medan"
        public List<GeoPosition> polyline; // Detailed polyline mengikuti jalan
        public double distanceKm;
        public boolean isOneWay;
        public String encodedPolyline;    // Encoded polyline untuk disimpan di database
        
        public RoadInfo() {
            this.polyline = new ArrayList<>();
            this.distanceKm = 0;
            this.isOneWay = false;
            this.roadName = "";
            this.fullName = "";
            this.encodedPolyline = "";
        }
    }
    
    /**
     * Get road info antara dua koordinat menggunakan Directions API
     * Ini akan mengembalikan nama jalan yang dilalui dan polyline yang mengikuti jalan
     */
    public RoadInfo getRoadInfo(double startLat, double startLng, double endLat, double endLng) {
        RoadInfo roadInfo = new RoadInfo();
        
        try {
            // Use Directions API untuk mendapatkan route dengan detailed steps
            // CRITICAL: Use Locale.US to ensure decimal points (not commas)
            String urlString = String.format(Locale.US,
                "https://maps.googleapis.com/maps/api/directions/json?origin=%.7f,%.7f&destination=%.7f,%.7f&mode=walking&region=ID&language=id&key=%s",
                startLat, startLng, endLat, endLng, API_KEY
            );
            
            logger.info("Fetching road info from Google Maps Directions API...");
            logger.info("URL: " + urlString);
            
            URI uri = new URI(urlString);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                logger.warning("Google Directions API returned status: " + responseCode);
                return roadInfo;
            }
            
            // Read response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            // Parse JSON response
            JSONObject json = new JSONObject(response.toString());
            
            // Check status
            String status = json.getString("status");
            if (!"OK".equals(status)) {
                logger.warning("Google Directions API status: " + status);
                return roadInfo;
            }
            
            // Get routes
            JSONArray routes = json.getJSONArray("routes");
            if (routes.length() == 0) {
                logger.warning("No routes found");
                return roadInfo;
            }
            
            // Get first route
            JSONObject route = routes.getJSONObject(0);
            
            // Get overview polyline (encoded)
            if (route.has("overview_polyline")) {
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                roadInfo.encodedPolyline = overviewPolyline.getString("points");
                
                // Decode polyline
                roadInfo.polyline = decodePolyline(roadInfo.encodedPolyline);
                logger.info("Decoded " + roadInfo.polyline.size() + " points from overview polyline");
            }
            
            // Get legs untuk extract road names
            JSONArray legs = route.getJSONArray("legs");
            if (legs.length() > 0) {
                JSONObject leg = legs.getJSONObject(0);
                
                // Get distance
                if (leg.has("distance")) {
                    JSONObject distance = leg.getJSONObject("distance");
                    roadInfo.distanceKm = distance.getDouble("value") / 1000.0;
                }
                
                // Extract road names from steps
                List<String> roadNames = new ArrayList<>();
                JSONArray steps = leg.getJSONArray("steps");
                
                for (int i = 0; i < steps.length(); i++) {
                    JSONObject step = steps.getJSONObject(i);
                    
                    // Get HTML instructions yang sering mengandung nama jalan
                    if (step.has("html_instructions")) {
                        String instructions = step.getString("html_instructions");
                        String extractedRoadName = extractRoadNameFromInstructions(instructions);
                        if (!extractedRoadName.isEmpty() && !roadNames.contains(extractedRoadName)) {
                            roadNames.add(extractedRoadName);
                        }
                    }
                    
                    // Try to get maneuver untuk detect one-way
                    if (step.has("maneuver")) {
                        String maneuver = step.getString("maneuver");
                        // Check for one-way indicators (simplified)
                        if (maneuver.contains("uturn") || maneuver.contains("ramp")) {
                            roadInfo.isOneWay = true;
                        }
                    }
                }
                
                // Set road name (gabungkan jika ada beberapa jalan)
                if (!roadNames.isEmpty()) {
                    roadInfo.roadName = roadNames.get(0); // Main road name
                    roadInfo.fullName = String.join(" → ", roadNames);
                    logger.info("Extracted road names: " + roadInfo.fullName);
                } else {
                    roadInfo.roadName = "Jalan Kampus USU";
                    roadInfo.fullName = "Jalan Kampus USU";
                }
            }
            
            logger.info(String.format("Road info fetched: %s (%.2f km, %d polyline points)", 
                roadInfo.roadName, roadInfo.distanceKm, roadInfo.polyline.size()));
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting road info from Google Maps", e);
        }
        
        return roadInfo;
    }
    
    /**
     * Extract road name dari HTML instructions
     * Contoh: "Head <b>north</b> on <b>Jl. Alumni</b>" -> "Jl. Alumni"
     */
    private String extractRoadNameFromInstructions(String htmlInstructions) {
        // Remove HTML tags
        String plainText = htmlInstructions.replaceAll("<.*?>", " ").trim();
        
        // Look for patterns like "on Jl. XXX" atau "onto Jl. XXX"
        String[] patterns = {
            " on ", " onto ", " di ", " ke ", " via ", " through "
        };
        
        for (String pattern : patterns) {
            int idx = plainText.toLowerCase().indexOf(pattern);
            if (idx >= 0) {
                String afterPattern = plainText.substring(idx + pattern.length()).trim();
                
                // Get road name (until next comma, slash, or end of string)
                String[] parts = afterPattern.split("[,/]");
                if (parts.length > 0) {
                    String roadName = parts[0].trim();
                    
                    // Clean up common suffixes
                    roadName = roadName.replaceAll("(?i)\\s*toward.*", "");
                    roadName = roadName.replaceAll("(?i)\\s*menuju.*", "");
                    
                    // Only return if it looks like a road name (starts with Jl. or contains common road words)
                    if (roadName.matches("(?i).*(jl|jalan|road|street|avenue).*") || 
                        roadName.length() > 3) {
                        return roadName;
                    }
                }
            }
        }
        
        return "";
    }
    
    /**
     * Snap coordinates ke jalan terdekat menggunakan Roads API
     * Ini akan mengembalikan koordinat yang benar-benar berada di jalan
     */
    public GeoPosition snapToRoad(double lat, double lng) {
        try {
            String urlString = String.format(
                "https://roads.googleapis.com/v1/nearestRoads?points=%f,%f&key=%s",
                lat, lng, API_KEY
            );
            
            URI uri = new URI(urlString);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                logger.warning("Google Roads API returned status: " + responseCode);
                return new GeoPosition(lat, lng); // Return original if failed
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            JSONObject json = new JSONObject(response.toString());
            
            if (json.has("snappedPoints")) {
                JSONArray snappedPoints = json.getJSONArray("snappedPoints");
                if (snappedPoints.length() > 0) {
                    JSONObject snapped = snappedPoints.getJSONObject(0);
                    JSONObject location = snapped.getJSONObject("location");
                    
                    double snappedLat = location.getDouble("latitude");
                    double snappedLng = location.getDouble("longitude");
                    
                    logger.info(String.format("Snapped (%.6f, %.6f) to road (%.6f, %.6f)", 
                        lat, lng, snappedLat, snappedLng));
                    
                    return new GeoPosition(snappedLat, snappedLng);
                }
            }
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error snapping to road", e);
        }
        
        return new GeoPosition(lat, lng); // Return original if failed
    }
    
    /**
     * Get road name at specific coordinate menggunakan Geocoding API
     */
    public String getRoadNameAtCoordinate(double lat, double lng) {
        try {
            String urlString = String.format(
                "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&result_type=route&key=%s",
                lat, lng, API_KEY
            );
            
            URI uri = new URI(urlString);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                logger.warning("Google Geocoding API returned status: " + responseCode);
                return "Jalan Kampus USU";
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            JSONObject json = new JSONObject(response.toString());
            
            if ("OK".equals(json.getString("status"))) {
                JSONArray results = json.getJSONArray("results");
                if (results.length() > 0) {
                    JSONObject result = results.getJSONObject(0);
                    
                    // Try to get route from address_components
                    if (result.has("address_components")) {
                        JSONArray components = result.getJSONArray("address_components");
                        for (int i = 0; i < components.length(); i++) {
                            JSONObject component = components.getJSONObject(i);
                            JSONArray types = component.getJSONArray("types");
                            
                            for (int j = 0; j < types.length(); j++) {
                                if ("route".equals(types.getString(j))) {
                                    return component.getString("long_name");
                                }
                            }
                        }
                    }
                    
                    // Fallback: use formatted_address
                    String address = result.getString("formatted_address");
                    String[] parts = address.split(",");
                    if (parts.length > 0) {
                        return parts[0].trim();
                    }
                }
            }
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error getting road name from geocoding", e);
        }
        
        return "Jalan Kampus USU";
    }
    
    /**
     * Decode Google Maps encoded polyline
     * Algorithm: https://developers.google.com/maps/documentation/utilities/polylinealgorithm
     */
    private List<GeoPosition> decodePolyline(String encoded) {
        List<GeoPosition> poly = new ArrayList<>();
        int index = 0;
        int len = encoded.length();
        int lat = 0;
        int lng = 0;
        
        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            
            shift = 0;
            result = 0;
            
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            
            double latitude = lat / 1E5;
            double longitude = lng / 1E5;
            
            poly.add(new GeoPosition(latitude, longitude));
        }
        
        return poly;
    }
    
    /**
     * Encode polyline (reverse of decode)
     * Untuk menyimpan ke database
     */
    public static String encodePolyline(List<GeoPosition> path) {
        StringBuilder encodedString = new StringBuilder();
        
        int prevLat = 0;
        int prevLng = 0;
        
        for (GeoPosition point : path) {
            int lat = (int) Math.round(point.getLatitude() * 1E5);
            int lng = (int) Math.round(point.getLongitude() * 1E5);
            
            encodedString.append(encodeValue(lat - prevLat));
            encodedString.append(encodeValue(lng - prevLng));
            
            prevLat = lat;
            prevLng = lng;
        }
        
        return encodedString.toString();
    }
    
    private static String encodeValue(int value) {
        value = value < 0 ? ~(value << 1) : (value << 1);
        
        StringBuilder encoded = new StringBuilder();
        while (value >= 0x20) {
            encoded.append(Character.toChars((0x20 | (value & 0x1f)) + 63));
            value >>= 5;
        }
        encoded.append(Character.toChars(value + 63));
        
        return encoded.toString();
    }
}
