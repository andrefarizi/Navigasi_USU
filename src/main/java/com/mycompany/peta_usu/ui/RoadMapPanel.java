package com.mycompany.peta_usu.ui;

import com.mycompany.peta_usu.dao.*;
import com.mycompany.peta_usu.models.*;
import com.mycompany.peta_usu.services.GoogleMapsRoadService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.sql.Timestamp;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.*;
import org.jxmapviewer.input.*;
import javax.swing.event.MouseInputListener;
import org.jxmapviewer.painter.Painter;
import java.awt.geom.Point2D;

/**
 * RoadMapPanel - Visual management untuk jalan dan penutupan jalan
 * Fitur:
 * - Jalan normal = hitam
 * - Jalan tertutup = merah
 * - One-way = panah satu arah
 * - Two-way = panah dua arah
 * - Klik jalan untuk edit status
 * 
 * @author PETA_USU Team
 */
public class RoadMapPanel extends JPanel {
    
    private RoadDAO roadDAO;
    private RoadClosureDAO closureDAO;
    private GoogleMapsRoadService roadService;
    private int currentUserId;
    
    // UI Components
    private JXMapViewer mapViewer;
    private JTable roadsTable;
    private DefaultTableModel tableModel;
    private JButton btnAddRoad;
    private JButton btnEditRoad;
    private JButton btnDeleteRoad;
    private JButton btnSetClosure;
    private JButton btnRefresh;
    private JButton btnFetchGoogleMaps;
    
    // Data
    private List<Road> allRoads;
    private Map<Integer, RoadClosure> activeClosures;
    private Road selectedRoad = null;
    
    public RoadMapPanel(int userId) {
        this.currentUserId = userId;
        this.roadDAO = new RoadDAO();
        this.closureDAO = new RoadClosureDAO();
        this.roadService = new GoogleMapsRoadService();
        this.activeClosures = new HashMap<>();
        
        initComponents();
        loadRoads();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel - Title
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center - Split pane (Map | Table)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createMapPanel());
        splitPane.setRightComponent(createTablePanel());
        splitPane.setDividerLocation(650);
        add(splitPane, BorderLayout.CENTER);
        
        // Bottom panel - Status
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(new Color(56, 136, 96)); // USU Green
        
        JLabel lblTitle = new JLabel("🛣️ Peta Jalan Kampus");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        panel.add(lblTitle);
        
        return panel;
    }
    
    private JPanel createMapPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Peta Jalan USU"));
        
        // Initialize JXMapViewer with Google Maps
        mapViewer = new JXMapViewer();
        TileFactoryInfo info = new TileFactoryInfo(0, 17, 17, 256, true, true,
                "http://mt0.google.com/vt/lyrs=m", "x", "y", "z") {
            @Override
            public String getTileUrl(int x, int y, int zoom) {
                zoom = this.getTotalMapZoom() - zoom;
                return String.format("https://mt0.google.com/vt/lyrs=m&x=%d&y=%d&z=%d", x, y, zoom);
            }
        };
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(8);
        mapViewer.setTileFactory(tileFactory);
        
        // Set center ke USU
        GeoPosition usuCenter = new GeoPosition(3.5651891, 98.6566015);
        mapViewer.setZoom(5); // Zoom level untuk kampus (lebih dekat)
        mapViewer.setAddressLocation(usuCenter);
        
        // Add mouse controls
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        
        // Add click listener untuk select road
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    Point point = e.getPoint();
                    GeoPosition geoPos = mapViewer.convertPointToGeoPosition(point);
                    selectNearestRoad(geoPos.getLatitude(), geoPos.getLongitude());
                }
            }
        });
        
        // Add mapViewer directly to panel (no JScrollPane needed)
        panel.add(mapViewer, BorderLayout.CENTER);
        
        // Legend panel
        JPanel legendPanel = createLegendPanel();
        panel.add(legendPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createLegendPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Legenda"));
        
        // Normal road
        panel.add(createLegendItem(Color.BLACK, "Jalan Normal"));
        
        // Closed road
        panel.add(createLegendItem(Color.RED, "Jalan Tertutup"));
        
        // One-way
        panel.add(createLegendItem(new Color(0, 100, 200), "→ Satu Arah"));
        
        // Two-way
        panel.add(createLegendItem(new Color(0, 150, 0), "↔ Dua Arah"));
        
        return panel;
    }
    
    private JPanel createLegendItem(Color color, String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setBackground(Color.WHITE);
        
        // Color box
        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(20, 10));
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        panel.add(colorBox);
        panel.add(new JLabel(text));
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Daftar Jalan"));
        
        // Table
        String[] columns = {"ID", "Nama Jalan", "Tipe", "Jarak (m)", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        roadsTable = new JTable(tableModel);
        roadsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roadsTable.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(roadsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons panel - gunakan GridLayout 2 baris agar semua button terlihat
        JPanel buttonsContainer = new JPanel(new BorderLayout());
        
        // Baris 1 - CRUD buttons
        JPanel buttonsRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        btnAddRoad = new JButton("➕ Tambah Jalan");
        btnAddRoad.setBackground(new Color(76, 175, 80));
        btnAddRoad.setForeground(Color.WHITE);
        btnAddRoad.setFocusPainted(false);
        btnAddRoad.addActionListener(e -> showAddRoadDialog());
        
        btnEditRoad = new JButton("✏️ Edit");
        btnEditRoad.setBackground(new Color(33, 150, 243));
        btnEditRoad.setForeground(Color.WHITE);
        btnEditRoad.setFocusPainted(false);
        btnEditRoad.addActionListener(e -> editSelectedRoad());
        
        btnDeleteRoad = new JButton("🗑️ Hapus");
        btnDeleteRoad.setBackground(new Color(244, 67, 54));
        btnDeleteRoad.setForeground(Color.WHITE);
        btnDeleteRoad.setFocusPainted(false);
        btnDeleteRoad.addActionListener(e -> deleteSelectedRoad());
        
        btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadRoads());
        
        buttonsRow1.add(btnAddRoad);
        buttonsRow1.add(btnEditRoad);
        buttonsRow1.add(btnDeleteRoad);
        buttonsRow1.add(btnRefresh);
        
        // Baris 2 - Google Maps & Closure buttons
        JPanel buttonsRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        btnFetchGoogleMaps = new JButton("🗺️ Fetch dari Google Maps");
        btnFetchGoogleMaps.setBackground(new Color(66, 133, 244)); // Google Blue
        btnFetchGoogleMaps.setForeground(Color.WHITE);
        btnFetchGoogleMaps.setFocusPainted(false);
        btnFetchGoogleMaps.setFont(new Font("Arial", Font.BOLD, 12));
        btnFetchGoogleMaps.setToolTipText("Ambil polyline dan nama jalan dari Google Maps API");
        btnFetchGoogleMaps.addActionListener(e -> fetchGoogleMapsData());
        
        btnSetClosure = new JButton("🚧 Atur Penutupan");
        btnSetClosure.setBackground(new Color(255, 152, 0));
        btnSetClosure.setForeground(Color.WHITE);
        btnSetClosure.setFocusPainted(false);
        btnSetClosure.addActionListener(e -> setRoadClosure());
        
        buttonsRow2.add(btnFetchGoogleMaps);
        buttonsRow2.add(btnSetClosure);
        
        // Combine rows
        JPanel buttonsCombined = new JPanel();
        buttonsCombined.setLayout(new BoxLayout(buttonsCombined, BoxLayout.Y_AXIS));
        buttonsCombined.add(buttonsRow1);
        buttonsCombined.add(buttonsRow2);
        
        buttonsContainer.add(buttonsCombined, BorderLayout.CENTER);
        panel.add(buttonsContainer, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.setBackground(new Color(240, 248, 255)); // Light blue background
        
        JLabel lblInfo = new JLabel(
            "<html>" +
            "<b>📍 Tips:</b> " +
            "Pilih jalan → Klik <b>'🗺️ Fetch dari Google Maps'</b> untuk mendapatkan rute sebenarnya | " +
            "Klik <b>'🚧 Atur Penutupan'</b> untuk tutup jalan (akan tampil MERAH di peta)" +
            "</html>"
        );
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        panel.add(lblInfo);
        
        return panel;
    }
    
    private void loadRoads() {
        allRoads = roadDAO.getAllRoads();
        List<RoadClosure> closures = closureDAO.getActiveClosures();
        
        // Build closure map
        activeClosures.clear();
        if (closures != null) {
            for (RoadClosure closure : closures) {
                activeClosures.put(closure.getRoadId(), closure);
            }
        }
        
        // Update table
        tableModel.setRowCount(0);
        if (allRoads != null) {
            for (Road road : allRoads) {
                String status = "Normal";
                if (activeClosures.containsKey(road.getRoadId())) {
                    RoadClosure closure = activeClosures.get(road.getRoadId());
                    status = closure.getClosureType().toString();
                }
                
                tableModel.addRow(new Object[]{
                    road.getRoadId(),
                    road.getRoadName(),
                    road.getRoadType(),
                    String.format("%.0f", road.getDistance()),
                    status
                });
            }
        }
        
        // Repaint map dengan roads
        repaintRoadsOnMap();
    }
    
    private void repaintRoadsOnMap() {
        if (allRoads == null || allRoads.isEmpty()) {
            return;
        }
        
        // Create custom painter untuk roads
        Painter<JXMapViewer> roadPainter = new Painter<JXMapViewer>() {
            @Override
            public void paint(Graphics2D g, JXMapViewer map, int width, int height) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                for (Road road : allRoads) {
                    // Determine color based on closure status
                    Color roadColor = Color.BLACK;
                    int strokeWidth = 4; // Increased for better visibility
                    
                    if (activeClosures.containsKey(road.getRoadId())) {
                        RoadClosure closure = activeClosures.get(road.getRoadId());
                        if (closure.getClosureType() == RoadClosure.ClosureType.PERMANENT) {
                            roadColor = new Color(220, 20, 60); // Red untuk jalan tertutup
                            strokeWidth = 5; // Lebih tebal untuk jalan tertutup
                        } else if (closure.getClosureType() == RoadClosure.ClosureType.TEMPORARY) {
                            roadColor = new Color(255, 140, 0); // Orange
                        }
                    }
                    
                    // Determine stroke based on is_one_way flag
                    BasicStroke stroke;
                    if (road.isOneWay()) {
                        // One way road - dashed blue line
                        if (activeClosures.containsKey(road.getRoadId())) {
                            // Keep closure color if closed
                        } else {
                            roadColor = new Color(0, 100, 200); // Blue
                        }
                        stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, 
                            BasicStroke.JOIN_ROUND, 0, new float[]{10, 5}, 0);
                    } else {
                        // Two way road - solid line
                        stroke = new BasicStroke(strokeWidth);
                    }
                    
                    g.setColor(roadColor);
                    g.setStroke(stroke);
                    
                    // Check if road has Google Maps polyline
                    if (road.getPolylinePoints() != null && !road.getPolylinePoints().isEmpty()) {
                        // Use detailed polyline from Google Maps
                        List<GeoPosition> polyline = decodePolyline(road.getPolylinePoints());
                        
                        if (polyline.size() > 1) {
                            // Draw polyline sebagai multi-segment line yang mengikuti jalan
                            for (int i = 0; i < polyline.size() - 1; i++) {
                                GeoPosition p1 = polyline.get(i);
                                GeoPosition p2 = polyline.get(i + 1);
                                
                                Point2D pt1 = map.getTileFactory().geoToPixel(p1, map.getZoom());
                                Point2D pt2 = map.getTileFactory().geoToPixel(p2, map.getZoom());
                                
                                Rectangle viewportBounds = map.getViewportBounds();
                                int x1 = (int)(pt1.getX() - viewportBounds.getX());
                                int y1 = (int)(pt1.getY() - viewportBounds.getY());
                                int x2 = (int)(pt2.getX() - viewportBounds.getX());
                                int y2 = (int)(pt2.getY() - viewportBounds.getY());
                                
                                g.drawLine(x1, y1, x2, y2);
                            }
                            
                            // Draw arrow at midpoint for direction
                            int midIdx = polyline.size() / 2;
                            if (midIdx > 0 && midIdx < polyline.size()) {
                                GeoPosition p1 = polyline.get(midIdx - 1);
                                GeoPosition p2 = polyline.get(midIdx);
                                
                                Point2D pt1 = map.getTileFactory().geoToPixel(p1, map.getZoom());
                                Point2D pt2 = map.getTileFactory().geoToPixel(p2, map.getZoom());
                                
                                Rectangle viewportBounds = map.getViewportBounds();
                                int x1 = (int)(pt1.getX() - viewportBounds.getX());
                                int y1 = (int)(pt1.getY() - viewportBounds.getY());
                                int x2 = (int)(pt2.getX() - viewportBounds.getX());
                                int y2 = (int)(pt2.getY() - viewportBounds.getY());
                                
                                if (road.isOneWay()) {
                                    drawArrow(g, x1, y1, x2, y2, roadColor);
                                } else {
                                    drawArrow(g, x1, y1, x2, y2, roadColor);
                                    drawArrow(g, x2, y2, x1, y1, roadColor);
                                }
                            }
                        }
                    } else {
                        // Fallback: Draw simple straight line (old behavior)
                        GeoPosition start = new GeoPosition(road.getStartLat(), road.getStartLng());
                        GeoPosition end = new GeoPosition(road.getEndLat(), road.getEndLng());
                        
                        Point2D startPoint = map.getTileFactory().geoToPixel(start, map.getZoom());
                        Point2D endPoint = map.getTileFactory().geoToPixel(end, map.getZoom());
                        
                        Rectangle viewportBounds = map.getViewportBounds();
                        int x1 = (int)(startPoint.getX() - viewportBounds.getX());
                        int y1 = (int)(startPoint.getY() - viewportBounds.getY());
                        int x2 = (int)(endPoint.getX() - viewportBounds.getX());
                        int y2 = (int)(endPoint.getY() - viewportBounds.getY());
                        
                        g.drawLine(x1, y1, x2, y2);
                        
                        // Draw arrows
                        if (road.isOneWay()) {
                            drawArrow(g, x1, y1, x2, y2, roadColor);
                        } else {
                            drawArrow(g, x1, y1, x2, y2, roadColor);
                            drawArrow(g, x2, y2, x1, y1, roadColor);
                        }
                    }
                    
                    // Highlight selected road
                    if (selectedRoad != null && selectedRoad.getRoadId() == road.getRoadId()) {
                        g.setColor(new Color(255, 255, 0, 150)); // Yellow highlight
                        g.setStroke(new BasicStroke(strokeWidth + 4));
                        
                        if (road.getPolylinePoints() != null && !road.getPolylinePoints().isEmpty()) {
                            List<GeoPosition> polyline = decodePolyline(road.getPolylinePoints());
                            for (int i = 0; i < polyline.size() - 1; i++) {
                                GeoPosition p1 = polyline.get(i);
                                GeoPosition p2 = polyline.get(i + 1);
                                
                                Point2D pt1 = map.getTileFactory().geoToPixel(p1, map.getZoom());
                                Point2D pt2 = map.getTileFactory().geoToPixel(p2, map.getZoom());
                                
                                Rectangle viewportBounds = map.getViewportBounds();
                                int x1 = (int)(pt1.getX() - viewportBounds.getX());
                                int y1 = (int)(pt1.getY() - viewportBounds.getY());
                                int x2 = (int)(pt2.getX() - viewportBounds.getX());
                                int y2 = (int)(pt2.getY() - viewportBounds.getY());
                                
                                g.drawLine(x1, y1, x2, y2);
                            }
                        } else {
                            GeoPosition start = new GeoPosition(road.getStartLat(), road.getStartLng());
                            GeoPosition end = new GeoPosition(road.getEndLat(), road.getEndLng());
                            
                            Point2D startPoint = map.getTileFactory().geoToPixel(start, map.getZoom());
                            Point2D endPoint = map.getTileFactory().geoToPixel(end, map.getZoom());
                            
                            Rectangle viewportBounds = map.getViewportBounds();
                            int x1 = (int)(startPoint.getX() - viewportBounds.getX());
                            int y1 = (int)(startPoint.getY() - viewportBounds.getY());
                            int x2 = (int)(endPoint.getX() - viewportBounds.getX());
                            int y2 = (int)(endPoint.getY() - viewportBounds.getY());
                            
                            g.drawLine(x1, y1, x2, y2);
                        }
                    }
                }
            }
        };
        
        mapViewer.setOverlayPainter(roadPainter);
        mapViewer.repaint();
    }
    
    /**
     * Decode Google Maps polyline
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
     * Draw arrow di ujung line
     */
    private void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2, Color color) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 10;
        
        // Arrow point di 80% dari garis
        int midX = (int)(x1 + 0.8 * (x2 - x1));
        int midY = (int)(y1 + 0.8 * (y2 - y1));
        
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        
        // Arrow tip
        xPoints[0] = midX;
        yPoints[0] = midY;
        
        // Left wing
        xPoints[1] = (int)(midX - arrowSize * Math.cos(angle - Math.PI / 6));
        yPoints[1] = (int)(midY - arrowSize * Math.sin(angle - Math.PI / 6));
        
        // Right wing
        xPoints[2] = (int)(midX - arrowSize * Math.cos(angle + Math.PI / 6));
        yPoints[2] = (int)(midY - arrowSize * Math.sin(angle + Math.PI / 6));
        
        g.setColor(color);
        g.fillPolygon(xPoints, yPoints, 3);
    }
    
    /**
     * Select nearest road to clicked position
     */
    private void selectNearestRoad(double lat, double lng) {
        if (allRoads == null || allRoads.isEmpty()) {
            return;
        }
        
        double minDistance = Double.MAX_VALUE;
        Road nearest = null;
        
        for (Road road : allRoads) {
            // Calculate distance to road (simplified: distance to midpoint)
            double midLat = (road.getStartLat() + road.getEndLat()) / 2;
            double midLng = (road.getStartLng() + road.getEndLng()) / 2;
            
            double distance = Math.sqrt(
                Math.pow(lat - midLat, 2) + Math.pow(lng - midLng, 2)
            );
            
            if (distance < minDistance && distance < 0.001) { // Threshold
                minDistance = distance;
                nearest = road;
            }
        }
        
        if (nearest != null) {
            selectedRoad = nearest;
            
            // Highlight in table
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if ((int)tableModel.getValueAt(i, 0) == nearest.getRoadId()) {
                    roadsTable.setRowSelectionInterval(i, i);
                    roadsTable.scrollRectToVisible(roadsTable.getCellRect(i, 0, true));
                    break;
                }
            }
            
            repaintRoadsOnMap();
            
            JOptionPane.showMessageDialog(this,
                "Jalan dipilih: " + nearest.getRoadName() + "\n" +
                "Tipe: " + nearest.getRoadType() + "\n" +
                "Klik 'Atur Penutupan' untuk mengubah status",
                "Jalan Terpilih",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showAddRoadDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Tambah Jalan Baru", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Nama Jalan
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nama Jalan:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextField txtNama = new JTextField(30);
        panel.add(txtNama, gbc);
        
        // Tipe Jalan
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(new JLabel("Tipe Jalan:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JComboBox<Road.RoadType> cboType = new JComboBox<>(Road.RoadType.values());
        panel.add(cboType, gbc);
        
        // One Way
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(new JLabel("Satu Arah:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JCheckBox chkOneWay = new JCheckBox("Jalan satu arah");
        panel.add(chkOneWay, gbc);
        
        // Start Coordinate Label
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        JLabel lblStart = new JLabel("Koordinat Awal:");
        lblStart.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblStart, gbc);
        
        // Start Latitude
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        panel.add(new JLabel("  Latitude:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextField txtStartLat = new JTextField("3.5651");
        panel.add(txtStartLat, gbc);
        
        // Start Longitude
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        panel.add(new JLabel("  Longitude:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextField txtStartLng = new JTextField("98.6566");
        panel.add(txtStartLng, gbc);
        
        // End Coordinate Label
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 3;
        JLabel lblEnd = new JLabel("Koordinat Akhir:");
        lblEnd.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblEnd, gbc);
        
        // End Latitude
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1;
        panel.add(new JLabel("  Latitude:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextField txtEndLat = new JTextField("3.5670");
        panel.add(txtEndLat, gbc);
        
        // End Longitude
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 1;
        panel.add(new JLabel("  Longitude:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextField txtEndLng = new JTextField("98.6580");
        panel.add(txtEndLng, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 1;
        panel.add(new JLabel("Deskripsi:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextArea txtDesc = new JTextArea(2, 30);
        txtDesc.setLineWrap(true);
        panel.add(new JScrollPane(txtDesc), gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 3;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnSave = new JButton("Simpan");
        btnSave.setBackground(new Color(76, 175, 80));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> {
            try {
                // Validasi input
                String roadName = txtNama.getText().trim();
                if (roadName.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                        "Nama jalan harus diisi!",
                        "Validasi Error",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                Road road = new Road();
                road.setRoadName(roadName);
                road.setRoadType((Road.RoadType) cboType.getSelectedItem());
                road.setStartLat(Double.parseDouble(txtStartLat.getText().trim()));
                road.setStartLng(Double.parseDouble(txtStartLng.getText().trim()));
                road.setEndLat(Double.parseDouble(txtEndLat.getText().trim()));
                road.setEndLng(Double.parseDouble(txtEndLng.getText().trim()));
                road.setOneWay(chkOneWay.isSelected());
                road.setDescription(txtDesc.getText().trim());
                
                // Calculate distance using Haversine formula
                double distance = calculateDistance(
                    road.getStartLat(), road.getStartLng(),
                    road.getEndLat(), road.getEndLng()
                );
                road.setDistance(distance);
                
                // Set default values untuk field baru (Google Maps akan diisi kemudian)
                road.setPolylinePoints(null);
                road.setGoogleRoadName(null);
                road.setRoadSegments(null);
                road.setLastGmapsUpdate(null);
                
                if (roadDAO.insertRoad(road)) {
                    JOptionPane.showMessageDialog(dialog,
                        "✅ Jalan berhasil ditambahkan!\n\n" +
                        "Tip: Pilih jalan ini dan klik 'Fetch dari Google Maps'\n" +
                        "untuk mendapatkan polyline yang mengikuti jalan sebenarnya.",
                        "Sukses",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadRoads();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "Gagal menambahkan jalan ke database.\n" +
                        "Periksa log untuk detail error.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Format koordinat tidak valid!\n\n" +
                    "Gunakan format angka desimal, contoh:\n" +
                    "Latitude: 3.5651\n" +
                    "Longitude: 98.6566",
                    "Format Error",
                    JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error tidak terduga: " + ex.getMessage() + "\n\n" +
                    "Pastikan semua field diisi dengan benar.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        JButton btnCancel = new JButton("Batal");
        btnCancel.addActionListener(e -> dialog.dispose());
        
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        panel.add(btnPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in meters
    }
    
    private void editSelectedRoad() {
        int selectedRow = roadsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Pilih jalan yang akan diedit",
                "Info",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int roadId = (int) tableModel.getValueAt(selectedRow, 0);
        Road road = roadDAO.getRoadById(roadId);
        
        if (road == null) {
            JOptionPane.showMessageDialog(this,
                "Jalan tidak ditemukan",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Show edit dialog with road type options
        String[] types = {"MAIN_ROAD", "ONE_WAY", "TWO_WAY", "PEDESTRIAN"};
        String newType = (String) JOptionPane.showInputDialog(
            this,
            "Pilih tipe jalan baru untuk: " + road.getRoadName(),
            "Edit Tipe Jalan",
            JOptionPane.QUESTION_MESSAGE,
            null,
            types,
            road.getRoadType()
        );
        
        if (newType != null && !newType.equals(road.getRoadType().toString())) {
            road.setRoadType(Road.RoadType.valueOf(newType));
            if (roadDAO.updateRoad(road)) {
                JOptionPane.showMessageDialog(this,
                    "Tipe jalan berhasil diubah!",
                    "Sukses",
                    JOptionPane.INFORMATION_MESSAGE);
                loadRoads();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Gagal mengubah tipe jalan",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void setRoadClosure() {
        int selectedRow = roadsTable.getSelectedRow();
        if (selectedRow < 0 && selectedRoad == null) {
            JOptionPane.showMessageDialog(this,
                "Pilih jalan terlebih dahulu\n(klik jalan di peta atau pilih dari tabel)",
                "Info",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int roadId;
        String roadName;
        
        if (selectedRow >= 0) {
            roadId = (int) tableModel.getValueAt(selectedRow, 0);
            roadName = (String) tableModel.getValueAt(selectedRow, 1);
        } else {
            roadId = selectedRoad.getRoadId();
            roadName = selectedRoad.getRoadName();
        }
        
        // Show closure dialog
        String[] options = {"Normal (Buka)", "Tertutup Sementara", "Tertutup Permanen", "Batal"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "Atur status untuk: " + roadName,
            "Pengaturan Penutupan Jalan",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (choice == 0) {
            // Normal - hapus closure jika ada
            if (activeClosures.containsKey(roadId)) {
                RoadClosure closure = activeClosures.get(roadId);
                closureDAO.deleteClosure(closure.getClosureId());
            }
            JOptionPane.showMessageDialog(this, "Jalan dibuka kembali", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } else if (choice == 1 || choice == 2) {
            // Add/update closure
            RoadClosure closure = activeClosures.get(roadId);
            if (closure == null) {
                closure = new RoadClosure();
                closure.setRoadId(roadId);
                closure.setCreatedBy(currentUserId);
            }
            
            closure.setClosureType(choice == 1 ? 
                RoadClosure.ClosureType.TEMPORARY : 
                RoadClosure.ClosureType.PERMANENT);
            closure.setReason("Diatur melalui Peta Jalan");
            closure.setStartDate(new java.sql.Date(System.currentTimeMillis()));
            closure.setEndDate(new java.sql.Date(System.currentTimeMillis() + 86400000L)); // +1 day
            
            if (activeClosures.containsKey(roadId)) {
                closureDAO.updateClosure(closure);
            } else {
                closureDAO.insertClosure(closure);
            }
            
            JOptionPane.showMessageDialog(this, 
                "Status jalan berhasil diubah!", 
                "Sukses", 
                JOptionPane.INFORMATION_MESSAGE);
        }
        
        loadRoads();
    }
    
    private void deleteSelectedRoad() {
        int selectedRow = roadsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Pilih jalan yang akan dihapus",
                "Info",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Apakah Anda yakin ingin menghapus jalan ini?",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            int roadId = (int) tableModel.getValueAt(selectedRow, 0);
            
            if (roadDAO.deleteRoad(roadId)) {
                JOptionPane.showMessageDialog(this,
                    "Jalan berhasil dihapus!",
                    "Sukses",
                    JOptionPane.INFORMATION_MESSAGE);
                loadRoads();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Gagal menghapus jalan",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Fetch Google Maps data untuk jalan yang dipilih
     * Mengambil polyline dan nama jalan dari Google Maps API
     */
    private void fetchGoogleMapsData() {
        int selectedRow = roadsTable.getSelectedRow();
        if (selectedRow < 0 && selectedRoad == null) {
            JOptionPane.showMessageDialog(this,
                "Pilih jalan terlebih dahulu untuk di-fetch dari Google Maps",
                "Info",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int roadId;
        Road road;
        
        if (selectedRow >= 0) {
            roadId = (int) tableModel.getValueAt(selectedRow, 0);
            road = roadDAO.getRoadById(roadId);
        } else {
            road = selectedRoad;
        }
        
        if (road == null) {
            JOptionPane.showMessageDialog(this,
                "Jalan tidak ditemukan",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Show progress dialog
        JDialog progressDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Mengambil data dari Google Maps...", true);
        progressDialog.setSize(400, 120);
        progressDialog.setLocationRelativeTo(this);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel lblStatus = new JLabel("Menghubungi Google Maps API...");
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblStatus, BorderLayout.CENTER);
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        panel.add(progressBar, BorderLayout.SOUTH);
        
        progressDialog.add(panel);
        
        // Fetch in background thread
        SwingWorker<GoogleMapsRoadService.RoadInfo, Void> worker = new SwingWorker<GoogleMapsRoadService.RoadInfo, Void>() {
            @Override
            protected GoogleMapsRoadService.RoadInfo doInBackground() throws Exception {
                return roadService.getRoadInfo(
                    road.getStartLat(), road.getStartLng(),
                    road.getEndLat(), road.getEndLng()
                );
            }
            
            @Override
            protected void done() {
                try {
                    GoogleMapsRoadService.RoadInfo roadInfo = get();
                    
                    if (roadInfo.polyline.isEmpty()) {
                        JOptionPane.showMessageDialog(RoadMapPanel.this,
                            "Tidak dapat mengambil data dari Google Maps.\n" +
                            "Mungkin koordinat tidak valid atau tidak ada jalan di lokasi tersebut.",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    } else {
                        // Update road dengan data dari Google Maps
                        road.setPolylinePoints(roadInfo.encodedPolyline);
                        road.setGoogleRoadName(roadInfo.roadName);
                        road.setDistance(roadInfo.distanceKm * 1000); // Convert to meters
                        road.setLastGmapsUpdate(new java.sql.Timestamp(System.currentTimeMillis()));
                        
                        if (roadDAO.updateRoad(road)) {
                            JOptionPane.showMessageDialog(RoadMapPanel.this,
                                String.format(
                                    "✅ Berhasil mengambil data dari Google Maps!\n\n" +
                                    "Nama Jalan: %s\n" +
                                    "Jarak: %.2f km\n" +
                                    "Polyline Points: %d titik\n\n" +
                                    "Data disimpan ke database.",
                                    roadInfo.roadName.isEmpty() ? "(tidak ditemukan)" : roadInfo.roadName,
                                    roadInfo.distanceKm,
                                    roadInfo.polyline.size()
                                ),
                                "Sukses",
                                JOptionPane.INFORMATION_MESSAGE);
                            
                            loadRoads(); // Refresh display
                        } else {
                            JOptionPane.showMessageDialog(RoadMapPanel.this,
                                "Gagal menyimpan data ke database",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(RoadMapPanel.this,
                        "Error: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressDialog.dispose();
                }
            }
        };
        
        worker.execute();
        progressDialog.setVisible(true); // Blocks until worker calls dispose()
    }
}
