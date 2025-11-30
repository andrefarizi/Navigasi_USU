package com.mycompany.peta_usu.services;

import com.mycompany.peta_usu.models.Road;
import com.mycompany.peta_usu.models.RoadClosure;
import com.mycompany.peta_usu.dao.RoadDAO;
import com.mycompany.peta_usu.dao.RoadClosureDAO;
import java.util.*;

/**
 * PathfindingService - Algoritma A* untuk mencari rute tercepat
 * Menggunakan database roads untuk pathfinding
 * 
 * @author PETA_USU Team
 */
public class PathfindingService {
    
    private RoadDAO roadDAO;
    private RoadClosureDAO closureDAO;
    
    public PathfindingService() {
        this.roadDAO = new RoadDAO();
        this.closureDAO = new RoadClosureDAO();
    }
    
    /**
     * Node untuk A* algorithm
     */
    private static class Node implements Comparable<Node> {
        double lat;
        double lng;
        double gCost; // Cost from start
        double hCost; // Heuristic cost to end
        double fCost; // gCost + hCost
        Node parent;
        Road road; // Road yang digunakan untuk sampai ke node ini
        
        public Node(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
            this.gCost = Double.MAX_VALUE;
            this.hCost = 0;
            this.fCost = Double.MAX_VALUE;
            this.parent = null;
        }
        
        @Override
        public int compareTo(Node other) {
            return Double.compare(this.fCost, other.fCost);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Node)) return false;
            Node other = (Node) obj;
            return Math.abs(this.lat - other.lat) < 0.0001 && 
                   Math.abs(this.lng - other.lng) < 0.0001;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(
                Math.round(lat * 10000), 
                Math.round(lng * 10000)
            );
        }
    }
    
    /**
     * Route result dengan polyline dan total jarak
     */
    public static class RouteResult {
        public List<LatLng> polyline;
        public List<Road> roadsUsed;
        public double totalDistanceKm;
        public int estimatedMinutes;
        
        public RouteResult() {
            this.polyline = new ArrayList<>();
            this.roadsUsed = new ArrayList<>();
            this.totalDistanceKm = 0;
            this.estimatedMinutes = 0;
        }
    }
    
    public static class LatLng {
        public double lat;
        public double lng;
        
        public LatLng(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }
    
    /**
     * Cari rute tercepat dari start ke end menggunakan A* algorithm
     */
    public RouteResult findShortestPath(double startLat, double startLng, 
                                       double endLat, double endLng) {
        RouteResult result = new RouteResult();
        
        // Get all roads dari database
        List<Road> allRoads = roadDAO.getAllRoads();
        if (allRoads == null || allRoads.isEmpty()) {
            System.err.println("No roads found in database");
            return result;
        }
        
        // Get active closures
        List<RoadClosure> closures = closureDAO.getActiveClosures();
        Set<Integer> closedRoadIds = new HashSet<>();
        if (closures != null) {
            for (RoadClosure closure : closures) {
                // Hanya jalan yang permanently closed yang tidak bisa dilewati
                if (closure.getClosureType() == RoadClosure.ClosureType.PERMANENT) {
                    closedRoadIds.add(closure.getRoadId());
                }
            }
        }
        
        // Filter roads yang tidak closed
        List<Road> availableRoads = new ArrayList<>();
        for (Road road : allRoads) {
            if (!closedRoadIds.contains(road.getRoadId())) {
                availableRoads.add(road);
            }
        }
        
        // Build graph dari roads
        Map<Node, List<Edge>> graph = buildGraph(availableRoads);
        
        // Find closest nodes to start and end
        Node startNode = findClosestNode(graph.keySet(), startLat, startLng);
        Node endNode = findClosestNode(graph.keySet(), endLat, endLng);
        
        if (startNode == null || endNode == null) {
            System.err.println("Could not find start or end node in road network");
            return result;
        }
        
        // Run A* algorithm
        List<Node> path = astar(graph, startNode, endNode);
        
        if (path == null || path.isEmpty()) {
            System.err.println("No path found");
            return result;
        }
        
        // Convert path to polyline
        result.polyline.add(new LatLng(startLat, startLng)); // Start point
        for (Node node : path) {
            result.polyline.add(new LatLng(node.lat, node.lng));
            if (node.road != null) {
                result.roadsUsed.add(node.road);
            }
        }
        result.polyline.add(new LatLng(endLat, endLng)); // End point
        
        // Calculate total distance
        result.totalDistanceKm = 0;
        for (int i = 0; i < result.polyline.size() - 1; i++) {
            LatLng p1 = result.polyline.get(i);
            LatLng p2 = result.polyline.get(i + 1);
            result.totalDistanceKm += haversineDistance(p1.lat, p1.lng, p2.lat, p2.lng);
        }
        
        // Estimasi waktu (asumsi jalan kaki 5 km/jam)
        result.estimatedMinutes = (int) Math.ceil(result.totalDistanceKm / 5.0 * 60);
        
        return result;
    }
    
    /**
     * Build graph dari list of roads
     */
    private Map<Node, List<Edge>> buildGraph(List<Road> roads) {
        Map<Node, List<Edge>> graph = new HashMap<>();
        
        for (Road road : roads) {
            Node startNode = new Node(road.getStartLat(), road.getStartLng());
            Node endNode = new Node(road.getEndLat(), road.getEndLng());
            
            // Add nodes to graph jika belum ada
            graph.putIfAbsent(startNode, new ArrayList<>());
            graph.putIfAbsent(endNode, new ArrayList<>());
            
            // Calculate distance
            double distance = haversineDistance(
                road.getStartLat(), road.getStartLng(),
                road.getEndLat(), road.getEndLng()
            );
            
            // Add edges based on road type
            if ("MAIN_ROAD".equals(road.getRoadType()) || 
                "TWO_WAY".equals(road.getRoadType())) {
                // Two way road
                graph.get(startNode).add(new Edge(endNode, distance, road));
                graph.get(endNode).add(new Edge(startNode, distance, road));
            } else if ("ONE_WAY".equals(road.getRoadType())) {
                // One way road (start -> end only)
                graph.get(startNode).add(new Edge(endNode, distance, road));
            }
        }
        
        return graph;
    }
    
    private static class Edge {
        Node target;
        double weight;
        Road road;
        
        Edge(Node target, double weight, Road road) {
            this.target = target;
            this.weight = weight;
            this.road = road;
        }
    }
    
    /**
     * A* algorithm implementation
     */
    private List<Node> astar(Map<Node, List<Edge>> graph, Node start, Node end) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Node> closedSet = new HashSet<>();
        
        start.gCost = 0;
        start.hCost = haversineDistance(start.lat, start.lng, end.lat, end.lng);
        start.fCost = start.hCost;
        openSet.add(start);
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            
            if (current.equals(end)) {
                // Path found! Reconstruct path
                return reconstructPath(current);
            }
            
            closedSet.add(current);
            
            // Get neighbors dari graph
            List<Edge> edges = graph.get(current);
            if (edges == null) continue;
            
            for (Edge edge : edges) {
                Node neighbor = edge.target;
                
                if (closedSet.contains(neighbor)) {
                    continue;
                }
                
                double tentativeGCost = current.gCost + edge.weight;
                
                if (tentativeGCost < neighbor.gCost) {
                    neighbor.parent = current;
                    neighbor.road = edge.road;
                    neighbor.gCost = tentativeGCost;
                    neighbor.hCost = haversineDistance(neighbor.lat, neighbor.lng, end.lat, end.lng);
                    neighbor.fCost = neighbor.gCost + neighbor.hCost;
                    
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }
        
        // No path found
        return null;
    }
    
    /**
     * Reconstruct path dari end ke start
     */
    private List<Node> reconstructPath(Node end) {
        List<Node> path = new ArrayList<>();
        Node current = end;
        
        while (current != null) {
            path.add(0, current); // Add at beginning
            current = current.parent;
        }
        
        return path;
    }
    
    /**
     * Find closest node in set to given coordinates
     */
    private Node findClosestNode(Set<Node> nodes, double lat, double lng) {
        Node closest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Node node : nodes) {
            double distance = haversineDistance(node.lat, node.lng, lat, lng);
            if (distance < minDistance) {
                minDistance = distance;
                closest = node;
            }
        }
        
        return closest;
    }
    
    /**
     * Calculate haversine distance between two points (in kilometers)
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth radius in kilometers
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}
