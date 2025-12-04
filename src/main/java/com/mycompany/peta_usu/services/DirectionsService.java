package com.mycompany.peta_usu.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jxmapviewer.viewer.GeoPosition;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DirectionsService - Service untuk mendapatkan rute dari Google Directions API
 * Menggunakan Google Maps Directions API untuk mendapatkan polyline rute yang mengikuti jalan
 * 
 * === 4 PILAR OOP YANG DITERAPKAN ===
 * 
 * 1. ENCAPSULATION (Enkapsulasi):
 *    - API_KEY PRIVATE STATIC FINAL (rahasia, tidak bisa diubah)
 *    - GoogleMapsRoadService instance PRIVATE
 *    - Method parseDirectionsResponse() PRIVATE (internal parsing)
 * 
 * 2. INHERITANCE (Pewarisan):
 *    - Inner class DirectionsResult bisa di-extend untuk custom result
 * 
 * 3. POLYMORPHISM (Polimorfisme):
 *    - Method getWalkingDirections() bisa dipanggil dengan koordinat berbeda
 *    - DirectionsResult.polyline bisa berisi 10 point atau 1000 point
 * 
 * 4. ABSTRACTION (Abstraksi):
 *    - Service ini ABSTRAKSI LENGKAP dari Google Directions API
 *    - Sembunyikan: HTTP request, JSON parsing, polyline decoding, error handling
 *    - User cukup: getWalkingDirections(lat1, lng1, lat2, lng2)
 *    - Hasil: DirectionsResult dengan polyline siap pakai di peta
 *    - Tidak perlu tahu: API key, URL format, response structure
 * 
 * @author PETA_USU Team
 */
public class DirectionsService {
    
    private static final Logger logger = Logger.getLogger(DirectionsService.class.getName());
    private static final String API_KEY = "AIzaSyBy-ugy58EBTMwG2TqtBVlPhR8oF3LeMhA";
    private GoogleMapsRoadService roadService;
    
    public DirectionsService() {
        this.roadService = new GoogleMapsRoadService();
    }
    
    /**
     * Route result dari Google Directions API
     */
    public static class DirectionsResult {
        public List<GeoPosition> polyline;
        public double distanceKm;
        public int durationMinutes;
        public String summary;
        public String roadName;           // Nama jalan yang dilalui (dari Google Maps)
        public String encodedPolyline;     // Encoded polyline untuk disimpan
        public List<String> roadNames;     // List semua nama jalan yang dilalui
        
        public DirectionsResult() {
            this.polyline = new ArrayList<>();
            this.distanceKm = 0;
            this.durationMinutes = 0;
            this.summary = "";
            this.roadName = "";
            this.encodedPolyline = "";
            this.roadNames = new ArrayList<>();
        }
    }
    
    /**
     * Get directions dari Google Directions API
     * Mode: walking (jalan kaki)
     */
    public DirectionsResult getWalkingDirections(double startLat, double startLng, 
                                                  double endLat, double endLng) {
        DirectionsResult result = new DirectionsResult();
        
        try {
            // Build URL untuk Directions API dengan region=ID untuk Indonesia
            // PENTING: Gunakan Locale.US untuk memastikan titik desimal, bukan koma!
            String urlString = String.format(Locale.US,
                "https://maps.googleapis.com/maps/api/directions/json?origin=%.7f,%.7f&destination=%.7f,%.7f&mode=walking&region=ID&language=id&key=%s",
                startLat, startLng, endLat, endLng, API_KEY
            );
            
            logger.info("========================================");
            logger.info("🗺️ GOOGLE DIRECTIONS API REQUEST");
            logger.info("From: (" + startLat + ", " + startLng + ")");
            logger.info("To: (" + endLat + ", " + endLng + ")");
            logger.info("URL: " + urlString);
            logger.info("========================================");
            
            // Make HTTP request
            URI uri = new URI(urlString);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000); // Increased timeout
            conn.setReadTimeout(15000);
            
            int responseCode = conn.getResponseCode();
            logger.info("Response Code: " + responseCode);
            
            if (responseCode != 200) {
                logger.warning("❌ Google Directions API returned HTTP status: " + responseCode);
                return result;
            }
            
            // Read response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            String responseBody = response.toString();
            logger.info("Response length: " + responseBody.length() + " characters");
            
            // Parse JSON response
            JSONObject json = new JSONObject(responseBody);
            
            // Check status
            String status = json.getString("status");
            logger.info("API Status: " + status);
            
            if (!"OK".equals(status)) {
                logger.warning("❌ Google Directions API status: " + status);
                if (json.has("error_message")) {
                    logger.warning("Error message: " + json.getString("error_message"));
                }
                
                // FALLBACK: Jika NOT_FOUND atau ZERO_RESULTS, gunakan garis lurus dengan waypoints
                if ("NOT_FOUND".equals(status) || "ZERO_RESULTS".equals(status)) {
                    logger.warning("⚠️ Using straight line fallback (API returned " + status + ")");
                    result.polyline.add(new GeoPosition(startLat, startLng));
                    result.polyline.add(new GeoPosition(endLat, endLng));
                    result.distanceKm = calculateStraightLineDistance(startLat, startLng, endLat, endLng);
                    result.durationMinutes = (int) (result.distanceKm * 12); // ~12 minutes per km walking
                    result.summary = "Rute langsung (Google Maps tidak menemukan jalan)";
                    result.roadNames.add("Rute langsung");
                    return result;
                }
                
                return result;
            }
            
            // Get routes array
            JSONArray routes = json.getJSONArray("routes");
            if (routes.length() == 0) {
                logger.warning("No routes found");
                return result;
            }
            
            // Get first route
            JSONObject route = routes.getJSONObject(0);
            
            // Get summary
            if (route.has("summary")) {
                result.summary = route.getString("summary");
            }
            
            // Get overview polyline (encoded) for saving to database
            if (route.has("overview_polyline")) {
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                result.encodedPolyline = overviewPolyline.getString("points");
            }
            
            // Get legs array
            JSONArray legs = route.getJSONArray("legs");
            if (legs.length() == 0) {
                logger.warning("No legs found in route");
                return result;
            }
            
            // Process each leg
            for (int i = 0; i < legs.length(); i++) {
                JSONObject leg = legs.getJSONObject(i);
                
                // Get distance and duration
                if (leg.has("distance")) {
                    JSONObject distance = leg.getJSONObject("distance");
                    result.distanceKm += distance.getDouble("value") / 1000.0; // Convert meters to km
                }
                
                if (leg.has("duration")) {
                    JSONObject duration = leg.getJSONObject("duration");
                    result.durationMinutes += duration.getInt("value") / 60; // Convert seconds to minutes
                }
                
                // Get steps to extract road names
                JSONArray steps = leg.getJSONArray("steps");
                
                for (int j = 0; j < steps.length(); j++) {
                    JSONObject step = steps.getJSONObject(j);
                    
                    // Extract road name from html_instructions
                    if (step.has("html_instructions")) {
                        String instructions = step.getString("html_instructions");
                        String roadName = extractRoadNameFromInstructions(instructions);
                        if (!roadName.isEmpty() && !result.roadNames.contains(roadName)) {
                            result.roadNames.add(roadName);
                        }
                    }
                    
                    // Get encoded polyline
                    if (step.has("polyline")) {
                        JSONObject polylineObj = step.getJSONObject("polyline");
                        String encodedPolyline = polylineObj.getString("points");
                        
                        // Decode polyline
                        List<GeoPosition> decodedPoints = decodePolyline(encodedPolyline);
                        result.polyline.addAll(decodedPoints);
                    }
                }
            }
            
            // Set main road name (first road found, or summary)
            if (!result.roadNames.isEmpty()) {
                result.roadName = result.roadNames.get(0);
            } else if (!result.summary.isEmpty()) {
                result.roadName = result.summary;
            }
            
            logger.info(String.format("Route found: %.2f km, %d minutes, %d points, roads: %s", 
                result.distanceKm, result.durationMinutes, result.polyline.size(), 
                String.join(", ", result.roadNames)));
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting directions from Google Maps", e);
        }
        
        return result;
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
                    
                    // Only return if it looks like a road name
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
     * Calculate straight line distance between two points using Haversine formula
     * @return distance in kilometers
     */
    private double calculateStraightLineDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Get driving directions (untuk kendaraan)
     */
    public DirectionsResult getDrivingDirections(double startLat, double startLng, 
                                                  double endLat, double endLng) {
        DirectionsResult result = new DirectionsResult();
        
        try {
            String urlString = String.format(
                "https://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&mode=driving&key=%s",
                startLat, startLng, endLat, endLng, API_KEY
            );
            
            // Implementation sama seperti getWalkingDirections
            // (Untuk simplifikasi, bisa reuse code dengan parameter mode)
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting driving directions", e);
        }
        
        return result;
    }
}
