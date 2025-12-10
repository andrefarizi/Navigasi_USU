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
 * === 4 PILAR OOP ===
 * 1. ENCAPSULATION: Field roadDAO, mapViewer, allRoads PRIVATE
 * 2. INHERITANCE: Extends JPanel (parent: javax.swing.JPanel)
 *    Mewarisi method dari JPanel:
 *    • setLayout() - set layout manager
 *    • add() - tambah component
 *    • setBackground() - set background color
 *    • repaint(), revalidate() - refresh UI
 *    - Inner class CustomMapMouseListener extends MouseAdapter (parent: java.awt.event.MouseAdapter)
 *      Mewarisi: mouseClicked(), mousePressed(), mouseReleased(), mouseEntered(), mouseExited()
 *    - Implements Painter<JXMapViewer> untuk custom map rendering
 * 3. POLYMORPHISM: Override paint() untuk custom road rendering
 * 4. ABSTRACTION: Method decodePolyline() sembunyikan Google algorithm
 * 
 * @author PETA_USU Team
 */
public class RoadMapPanel extends JPanel {  // ← INHERITANCE dari javax.swing.JPanel
    
    // ========== ENCAPSULATION: Database & Services PRIVATE ==========
    private RoadDAO roadDAO;                      // ← PRIVATE: Database access
    private RoadClosureDAO closureDAO;            // ← PRIVATE: Database access
    private GoogleMapsRoadService roadService;    // ← PRIVATE: Google API
    private int currentUserId;                    // ← PRIVATE: Session user
    
    // ========== ENCAPSULATION: UI Components PRIVATE ==========
    private JXMapViewer mapViewer;                // ← PRIVATE: Map widget
    private JTable roadsTable;                    // ← PRIVATE: Table
    private DefaultTableModel tableModel;         // ← PRIVATE: Data model
    private JButton btnAddRoad;                   // ← PRIVATE: Buttons
    private JButton btnEditRoad;
    private JButton btnDeleteRoad;
    private JButton btnSetClosure;
    private JButton btnRefresh;
    private JButton btnFetchGoogleMaps;
    
    // Data
    private List<Road> allRoads;
    private Map<Integer, RoadClosure> activeClosures;
    private Road selectedRoad = null;
    
    // Draggable road markers
    private GeoPosition startMarker = null;
    private GeoPosition endMarker = null;
    private GeoPosition draggedMarker = null;
    private boolean isAddingRoad = false;
    private boolean isEditingRoadPosition = false;
    private Road roadBeingEdited = null;
    private javax.swing.Timer repaintTimer = null;
    private Road.RoadType selectedRoadType = Road.RoadType.NORMAL; // Track selected road type
    
    // Marker overlay panel
    private JPanel markerOverlay = null;
    
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
        // ========== POLYMORPHISM: Anonymous TileFactoryInfo ==========
        TileFactoryInfo info = new TileFactoryInfo(0, 17, 17, 256, true, true,
                "http://mt0.google.com/vt/lyrs=m", "x", "y", "z") {
            @Override  // ← POLYMORPHISM: Override getTileUrl untuk Google Maps tiles
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
        
        // Add mouse wheel zoom
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        
        // Setup overlay painter FIRST before mouse listeners
        // ========== POLYMORPHISM: Anonymous Painter<JXMapViewer> ==========
        mapViewer.setOverlayPainter(new Painter<JXMapViewer>() {
            @Override  // ← POLYMORPHISM: Override paint untuk draw roads
            public void paint(Graphics2D g, JXMapViewer map, int width, int height) {
                System.out.println("🖌️ OVERLAY PAINTER CALLED!");
                g = (Graphics2D) g.create();
                
                // Enable anti-aliasing
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Convert to world bitmap
                Rectangle rect = map.getViewportBounds();
                g.translate(-rect.x, -rect.y);
                
                // Draw all roads
                if (allRoads != null) {
                    for (Road road : allRoads) {
                        drawRoad(g, map, road);
                    }
                }
                
                // RESET translation for markers (they use screen coordinates)
                g.translate(rect.x, rect.y);
                
                // Draw draggable markers saat adding/editing road
                if (RoadMapPanel.this.isAddingRoad || RoadMapPanel.this.isEditingRoadPosition) {
                    if (RoadMapPanel.this.startMarker != null) {
                        drawDraggableMarker(g, map, RoadMapPanel.this.startMarker, "START", new Color(76, 175, 80)); // Green
                    }
                    if (RoadMapPanel.this.endMarker != null) {
                        drawDraggableMarker(g, map, RoadMapPanel.this.endMarker, "END", new Color(244, 67, 54)); // Red
                    }
                    
                    // Draw line between markers
                    if (startMarker != null && endMarker != null) {
                        Point2D p1 = map.getTileFactory().geoToPixel(startMarker, map.getZoom());
                        Point2D p2 = map.getTileFactory().geoToPixel(endMarker, map.getZoom());
                        
                        // Convert to screen coordinates
                        int x1 = (int)p1.getX() - rect.x;
                        int y1 = (int)p1.getY() - rect.y;
                        int x2 = (int)p2.getX() - rect.x;
                        int y2 = (int)p2.getY() - rect.y;
                        
                        // Set color based on selected road type
                        Color lineColor;
                        if (selectedRoadType == Road.RoadType.CLOSED) {
                            lineColor = new Color(244, 67, 54, 180); // Red for closed road
                        } else {
                            lineColor = new Color(66, 133, 244, 128); // Blue for normal/other roads
                        }
                        
                        g.setColor(lineColor);
                        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 
                            1.0f, new float[]{10, 5}, 0));
                        g.drawLine(x1, y1, x2, y2);
                        
                        // Draw distance label
                        double distance = calculateDistance(
                            startMarker.getLatitude(), startMarker.getLongitude(),
                            endMarker.getLatitude(), endMarker.getLongitude()
                        ) / 1000.0; // to km
                        
                        int midX = (x1 + x2) / 2;
                        int midY = (y1 + y2) / 2;
                        
                        String distText = String.format("%.2f km", distance);
                        g.setColor(Color.WHITE);
                        g.setFont(new Font("Arial", Font.BOLD, 12));
                        FontMetrics fm = g.getFontMetrics();
                        int textWidth = fm.stringWidth(distText);
                        g.fillRect(midX - textWidth/2 - 3, midY - fm.getHeight()/2 - 2, 
                            textWidth + 6, fm.getHeight() + 4);
                        g.setColor(selectedRoadType == Road.RoadType.CLOSED ? 
                            new Color(244, 67, 54) : new Color(66, 133, 244));
                        g.drawString(distText, midX - textWidth/2, midY + fm.getAscent()/2);
                    }
                }
                
                g.dispose();
            }
        });
        
        // Add custom mouse listener untuk drag markers DAN pan map
        CustomMapMouseListener customListener = new CustomMapMouseListener(mapViewer);
        mapViewer.addMouseListener(customListener);
        mapViewer.addMouseMotionListener(customListener);
        
        // Create transparent overlay panel for markers (LAYERED approach)
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        
        // Add map as bottom layer
        mapViewer.setAlignmentX(0.5f);
        mapViewer.setAlignmentY(0.5f);
        layeredPane.add(mapViewer, JLayeredPane.DEFAULT_LAYER);
        
        // Create marker overlay panel
        markerOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics gr) {
                super.paintComponent(gr);
                if (isAddingRoad || isEditingRoadPosition) {
                    Graphics2D g = (Graphics2D) gr.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    Rectangle rect = mapViewer.getViewportBounds();
                    
                    // Draw markers
                    if (startMarker != null) {
                        drawMarkerOnOverlay(g, mapViewer, startMarker, "START", new Color(76, 175, 80), rect);
                    }
                    if (endMarker != null) {
                        drawMarkerOnOverlay(g, mapViewer, endMarker, "END", new Color(244, 67, 54), rect);
                    }
                    
                    g.dispose();
                }
            }
        };
        markerOverlay.setOpaque(false);
        markerOverlay.setAlignmentX(0.5f);
        markerOverlay.setAlignmentY(0.5f);
        
        // Add mouse listener to overlay for proper coordinate handling
        MouseAdapter overlayMouseListener = new MouseAdapter() {
            private GeoPosition draggedMarkerRef = null;
            private Point lastDragPoint = null;
            
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isAddingRoad && !isEditingRoadPosition) {
                    // Pass event to map viewer for pan/zoom
                    Point mapPoint = SwingUtilities.convertPoint(markerOverlay, e.getPoint(), mapViewer);
                    mapViewer.dispatchEvent(new MouseEvent(mapViewer, e.getID(), e.getWhen(), 
                        e.getModifiersEx(), mapPoint.x, mapPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
                    return;
                }
                
                // Convert overlay coordinates to map viewer coordinates
                Point mapPoint = SwingUtilities.convertPoint(markerOverlay, e.getPoint(), mapViewer);
                GeoPosition clickPos = mapViewer.convertPointToGeoPosition(mapPoint);
                
                System.out.println("🖱️ CLICK: overlay=" + e.getPoint() + " -> map=" + mapPoint + " -> geo=(" + String.format("%.6f, %.6f", clickPos.getLatitude(), clickPos.getLongitude()) + ")");
                
                // Check if clicking near markers (with larger hit zone for easier grabbing)
                if (startMarker != null && isNearMarker(clickPos, startMarker)) {
                    draggedMarkerRef = startMarker;
                    lastDragPoint = mapPoint;
                    System.out.println("🟢 START GRABBED at: " + String.format("%.6f, %.6f", startMarker.getLatitude(), startMarker.getLongitude()));
                    e.consume(); // Don't pass to map
                } else if (endMarker != null && isNearMarker(clickPos, endMarker)) {
                    draggedMarkerRef = endMarker;
                    lastDragPoint = mapPoint;
                    System.out.println("🔴 END GRABBED at: " + String.format("%.6f, %.6f", endMarker.getLatitude(), endMarker.getLongitude()));
                    e.consume(); // Don't pass to map
                } else {
                    // Not clicking marker, pass to map for pan
                    mapViewer.dispatchEvent(new MouseEvent(mapViewer, e.getID(), e.getWhen(), 
                        e.getModifiersEx(), mapPoint.x, mapPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
                }
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedMarkerRef != null) {
                    // Convert overlay coordinates to map viewer coordinates
                    Point mapPoint = SwingUtilities.convertPoint(markerOverlay, e.getPoint(), mapViewer);
                    GeoPosition newPos = mapViewer.convertPointToGeoPosition(mapPoint);
                    
                    // Update marker position directly
                    if (draggedMarkerRef == startMarker) {
                        startMarker = newPos;
                        draggedMarkerRef = startMarker; // Update reference
                        if (lastDragPoint == null || mapPoint.distance(lastDragPoint) > 5) {
                            System.out.println("🟢 DRAGGING: " + String.format("%.6f, %.6f", newPos.getLatitude(), newPos.getLongitude()));
                            lastDragPoint = mapPoint;
                        }
                    } else if (draggedMarkerRef == endMarker) {
                        endMarker = newPos;
                        draggedMarkerRef = endMarker; // Update reference
                        if (lastDragPoint == null || mapPoint.distance(lastDragPoint) > 5) {
                            System.out.println("🔴 DRAGGING: " + String.format("%.6f, %.6f", newPos.getLatitude(), newPos.getLongitude()));
                            lastDragPoint = mapPoint;
                        }
                    }
                    
                    // Repaint overlay immediately for smooth visual update
                    markerOverlay.repaint();
                    e.consume(); // Don't pass to map
                } else {
                    // Pass drag event to map for panning
                    Point mapPoint = SwingUtilities.convertPoint(markerOverlay, e.getPoint(), mapViewer);
                    mapViewer.dispatchEvent(new MouseEvent(mapViewer, e.getID(), e.getWhen(), 
                        e.getModifiersEx(), mapPoint.x, mapPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (draggedMarkerRef != null) {
                    // Convert overlay coordinates to map viewer coordinates
                    Point mapPoint = SwingUtilities.convertPoint(markerOverlay, e.getPoint(), mapViewer);
                    GeoPosition finalPos = mapViewer.convertPointToGeoPosition(mapPoint);
                    
                    // Save final position
                    if (draggedMarkerRef == startMarker) {
                        startMarker = finalPos;
                        System.out.println("✅ START RELEASED at: " + String.format("%.6f, %.6f", finalPos.getLatitude(), finalPos.getLongitude()));
                    } else if (draggedMarkerRef == endMarker) {
                        endMarker = finalPos;
                        System.out.println("✅ END RELEASED at: " + String.format("%.6f, %.6f", finalPos.getLatitude(), finalPos.getLongitude()));
                    }
                    
                    draggedMarkerRef = null;
                    lastDragPoint = null;
                    markerOverlay.repaint();
                    e.consume();
                } else {
                    // Pass event to map
                    Point mapPoint = SwingUtilities.convertPoint(markerOverlay, e.getPoint(), mapViewer);
                    mapViewer.dispatchEvent(new MouseEvent(mapViewer, e.getID(), e.getWhen(), 
                        e.getModifiersEx(), mapPoint.x, mapPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                // Always pass mouseMoved to map (for cursor updates, etc.)
                Point mapPoint = SwingUtilities.convertPoint(markerOverlay, e.getPoint(), mapViewer);
                mapViewer.dispatchEvent(new MouseEvent(mapViewer, e.getID(), e.getWhen(), 
                    e.getModifiersEx(), mapPoint.x, mapPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
            }
        };
        
        markerOverlay.addMouseListener(overlayMouseListener);
        markerOverlay.addMouseMotionListener(overlayMouseListener);
        
        // Add mouse wheel listener for zoom
        markerOverlay.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // Pass wheel events to map for zoom
                Point mapPoint = SwingUtilities.convertPoint(markerOverlay, e.getPoint(), mapViewer);
                mapViewer.dispatchEvent(new MouseWheelEvent(mapViewer, e.getID(), e.getWhen(),
                    e.getModifiersEx(), mapPoint.x, mapPoint.y, e.getClickCount(), 
                    e.isPopupTrigger(), e.getScrollType(), e.getScrollAmount(), e.getWheelRotation()));
            }
        });
        
        layeredPane.add(markerOverlay, JLayeredPane.PALETTE_LAYER);
        
        // Add layered pane to panel
        panel.add(layeredPane, BorderLayout.CENTER);
        
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
        
        btnSetClosure = new JButton("🚧 Penutupan Jalan");
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
                    // Determine color based on closure status OR road type
                    Color roadColor = Color.BLACK;
                    int strokeWidth = 4; // Increased for better visibility
                    
                    // Check if road type is CLOSED
                    if (road.getRoadType() == Road.RoadType.CLOSED) {
                        roadColor = new Color(220, 20, 60); // Red untuk jalan tertutup
                        strokeWidth = 5; // Lebih tebal untuk jalan tertutup
                    } else if (activeClosures.containsKey(road.getRoadId())) {
                        RoadClosure closure = activeClosures.get(road.getRoadId());
                        if (closure.getClosureType() == RoadClosure.ClosureType.PERMANENT) {
                            roadColor = new Color(220, 20, 60); // Red untuk jalan tertutup
                            strokeWidth = 5; // Lebih tebal untuk jalan tertutup
                        } else if (closure.getClosureType() == RoadClosure.ClosureType.TEMPORARY) {
                            roadColor = new Color(255, 140, 0); // Orange
                        }
                    }
                    
                    // Determine stroke based on road type
                    BasicStroke stroke;
                    if (road.getRoadType() == Road.RoadType.ONE_WAY) {
                        // One way road - dashed line
                        if (road.getRoadType() != Road.RoadType.CLOSED && !activeClosures.containsKey(road.getRoadId())) {
                            roadColor = new Color(0, 100, 200); // Blue hanya jika tidak tertutup
                        }
                        stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, 
                            BasicStroke.JOIN_ROUND, 0, new float[]{10, 5}, 0);
                    } else if (road.getRoadType() == Road.RoadType.TWO_WAY) {
                        // Two way road - dashed green line
                        if (road.getRoadType() != Road.RoadType.CLOSED && !activeClosures.containsKey(road.getRoadId())) {
                            roadColor = new Color(34, 139, 34); // Green hanya jika tidak tertutup
                        }
                        stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, 
                            BasicStroke.JOIN_ROUND, 0, new float[]{10, 5}, 0);
                    } else {
                        // Normal road or closed road - solid line
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
        // Aktifkan mode drag marker
        isAddingRoad = true;
        System.out.println("========================================");
        System.out.println("🗺️ MODE TAMBAH JALAN AKTIF");
        System.out.println("========================================");
        
        // Set default posisi 2 marker di tengah peta
        GeoPosition center = mapViewer.getCenterPosition();
        double offsetLat = 0.002; // ~200 meter offset
        double offsetLng = 0.004;
        
        startMarker = new GeoPosition(center.getLatitude() + offsetLat, center.getLongitude() - offsetLng);
        endMarker = new GeoPosition(center.getLatitude() - offsetLat, center.getLongitude() + offsetLng);
        
        System.out.println("🟢 START Marker: " + startMarker.getLatitude() + ", " + startMarker.getLongitude());
        System.out.println("🔴 END Marker: " + endMarker.getLatitude() + ", " + endMarker.getLongitude());
        System.out.println("📍 Silakan geser marker HIJAU dan MERAH di peta!");
        
        // Start repaint timer ONLY during add mode
        if (repaintTimer != null) repaintTimer.stop();
        repaintTimer = new javax.swing.Timer(100, e -> mapViewer.repaint()); // 10 FPS
        repaintTimer.start();
        
        // Force immediate repaint
        mapViewer.repaint();
        
        // Show konfirmasi dialog
        showRoadConfirmationDialog();
    }
    
    private void showRoadConfirmationDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Konfirmasi Jalan Baru", false); // Non-modal agar bisa drag marker
        dialog.setSize(500, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setAlwaysOnTop(true); // Tetap di atas
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Info panel di atas
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(66, 133, 244));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel lblInfo = new JLabel(
            "<html><b style='color:white; font-size:12px;'>" +
            "🗺️ Geser Marker HIJAU (awal) dan MERAH (akhir) di peta!<br>" +
            "Klik dan tahan marker, lalu drag ke posisi yang diinginkan.</b></html>"
        );
        infoPanel.add(lblInfo);
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        
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
        cboType.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Road.RoadType) {
                    setText(((Road.RoadType) value).getDisplayName());
                }
                return this;
            }
        });
        cboType.addActionListener(e -> {
            selectedRoadType = (Road.RoadType) cboType.getSelectedItem();
            mapViewer.repaint(); // Update preview line color
            if (markerOverlay != null) markerOverlay.repaint();
        });
        panel.add(cboType, gbc);
        
        // Jarak Manual (km)
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(new JLabel("Jarak (km):"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JPanel distancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JTextField txtDistance = new JTextField("0.0", 10);
        distancePanel.add(txtDistance);
        JLabel lblDistanceHint = new JLabel("(akan dihitung otomatis jika 0)");
        lblDistanceHint.setFont(new Font("Arial", Font.ITALIC, 10));
        lblDistanceHint.setForeground(Color.GRAY);
        distancePanel.add(lblDistanceHint);
        panel.add(distancePanel, gbc);
        
        // Koordinat info (read-only, update otomatis)
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        JLabel lblCoordInfo = new JLabel("<html>📍 <i>Koordinat akan diambil dari posisi marker di peta</i></html>");
        lblCoordInfo.setFont(new Font("Arial", Font.PLAIN, 10));
        lblCoordInfo.setForeground(Color.GRAY);
        panel.add(lblCoordInfo, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        panel.add(new JLabel("Deskripsi:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextArea txtDesc = new JTextArea(3, 30);
        txtDesc.setLineWrap(true);
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        panel.add(scrollDesc, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnSave = new JButton("💾 Simpan Jalan");
        btnSave.setBackground(new Color(76, 175, 80));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
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
                road.setStartLat(startMarker.getLatitude());
                road.setStartLng(startMarker.getLongitude());
                road.setEndLat(endMarker.getLatitude());
                road.setEndLng(endMarker.getLongitude());
                
                // Set isOneWay based on road type
                road.setOneWay(road.getRoadType() == Road.RoadType.ONE_WAY);
                road.setDescription(txtDesc.getText().trim());
                
                // Use manual distance or calculate
                double manualDistance = Double.parseDouble(txtDistance.getText().trim());
                if (manualDistance > 0) {
                    road.setDistance(manualDistance * 1000); // Convert km to meters
                } else {
                    // Calculate distance using Haversine formula
                    double distance = calculateDistance(
                        road.getStartLat(), road.getStartLng(),
                        road.getEndLat(), road.getEndLng()
                    );
                    road.setDistance(distance);
                }
                
                // Set default values untuk field baru (Google Maps akan diisi kemudian)
                road.setPolylinePoints(null);
                road.setGoogleRoadName(null);
                road.setRoadSegments(null);
                road.setLastGmapsUpdate(null);
                
                if (roadDAO.insertRoad(road)) {
                    JOptionPane.showMessageDialog(dialog,
                        String.format(
                            "✅ Jalan berhasil ditambahkan!\n\n" +
                            "Nama: %s\n" +
                            "Koordinat Awal: %.6f, %.6f\n" +
                            "Koordinat Akhir: %.6f, %.6f\n\n" +
                            "Tip: Pilih jalan ini dan klik 'Fetch dari Google Maps'\n" +
                            "untuk mendapatkan polyline yang mengikuti jalan sebenarnya.",
                            roadName,
                            road.getStartLat(), road.getStartLng(),
                            road.getEndLat(), road.getEndLng()
                        ),
                        "Sukses",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    // Reset mode
                    isAddingRoad = false;
                    startMarker = null;
                    endMarker = null;
                    if (repaintTimer != null) {
                        repaintTimer.stop();
                        repaintTimer = null;
                    }
                    mapViewer.repaint();
                    
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
                    "Format jarak tidak valid!",
                    "Format Error",
                    JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        JButton btnCancel = new JButton("❌ Batal");
        btnCancel.addActionListener(e -> {
            isAddingRoad = false;
            startMarker = null;
            endMarker = null;
            if (repaintTimer != null) {
                repaintTimer.stop();
                repaintTimer = null;
            }
            mapViewer.repaint();
            dialog.dispose();
        });
        
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        panel.add(btnPanel, gbc);
        
        mainPanel.add(panel, BorderLayout.CENTER);
        dialog.add(mainPanel);
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
        
        // Simple edit dialog - hanya nama dan tipe
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Edit Jalan: " + road.getRoadName(), true);
        dialog.setSize(400, 200);
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
        JTextField txtNama = new JTextField(road.getRoadName(), 25);
        panel.add(txtNama, gbc);
        
        // Tipe Jalan
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(new JLabel("Tipe Jalan:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JComboBox<Road.RoadType> cboType = new JComboBox<>(Road.RoadType.values());
        cboType.setSelectedItem(road.getRoadType());
        cboType.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Road.RoadType) {
                    setText(((Road.RoadType) value).getDisplayName());
                }
                return this;
            }
        });
        cboType.addActionListener(e -> {
            selectedRoadType = (Road.RoadType) cboType.getSelectedItem();
            mapViewer.repaint(); // Update preview line color
            if (markerOverlay != null) markerOverlay.repaint();
        });
        panel.add(cboType, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnSave = new JButton("💾 Simpan");
        btnSave.setBackground(new Color(76, 175, 80));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> {
            String newName = txtNama.getText().trim();
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Nama jalan tidak boleh kosong!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            road.setRoadName(newName);
            road.setRoadType((Road.RoadType) cboType.getSelectedItem());
            road.setOneWay(road.getRoadType() == Road.RoadType.ONE_WAY);
            
            if (roadDAO.updateRoad(road)) {
                JOptionPane.showMessageDialog(dialog,
                    "✅ Jalan berhasil diupdate!",
                    "Sukses",
                    JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadRoads();
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Gagal mengupdate jalan",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
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
    
    /**
     * Check if click position is near a marker (within ~50 pixels)
     */
    private boolean isNearMarker(GeoPosition clickPos, GeoPosition markerPos) {
        Point2D clickPoint = mapViewer.getTileFactory().geoToPixel(clickPos, mapViewer.getZoom());
        Point2D markerPoint = mapViewer.getTileFactory().geoToPixel(markerPos, mapViewer.getZoom());
        
        double distance = Math.sqrt(
            Math.pow(clickPoint.getX() - markerPoint.getX(), 2) +
            Math.pow(clickPoint.getY() - markerPoint.getY(), 2)
        );
        
        return distance < 50; // 50 pixels threshold
    }
    
    /**
     * Draw draggable marker on overlay panel (VISIBLE APPROACH)
     */
    private void drawMarkerOnOverlay(Graphics2D g, JXMapViewer map, GeoPosition pos, String label, Color color, Rectangle viewport) {
        Point2D point = map.getTileFactory().geoToPixel(pos, map.getZoom());
        
        // Convert to overlay coordinates (relative to overlay panel, not viewport)
        int x = (int) point.getX() - viewport.x;
        int y = (int) point.getY() - viewport.y;
        
        // Draw small dot marker (14px)
        int pinSize = 14;
        int innerSize = 8;
        
        // Outer circle
        g.setColor(color);
        g.fillOval(x - pinSize/2, y - pinSize, pinSize, pinSize);
        
        // Inner circle
        g.setColor(Color.WHITE);
        g.fillOval(x - innerSize/2, y - pinSize + (pinSize - innerSize)/2, innerSize, innerSize);
        
        // Pointer (small)
        int[] xPoints = {x, x - 4, x + 4};
        int[] yPoints = {y, y - 4, y - 4};
        g.setColor(color);
        g.fillPolygon(xPoints, yPoints, 3);
        
        // Label (compact)
        g.setFont(new Font("Arial", Font.BOLD, 9));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(label);
        
        g.setColor(color);
        g.fillRoundRect(x - textWidth/2 - 2, y + 2, textWidth + 4, fm.getHeight() + 1, 3, 3);
        
        g.setColor(Color.WHITE);
        g.drawString(label, x - textWidth/2, y + 2 + fm.getAscent());
    }
    
    /**
     * Draw draggable marker on map (using SCREEN coordinates - viewport already adjusted)
     */
    private void drawDraggableMarker(Graphics2D g, JXMapViewer map, GeoPosition pos, String label, Color color) {
        // Convert geo to pixel (world coordinates)
        Point2D point = map.getTileFactory().geoToPixel(pos, map.getZoom());
        
        // Convert to screen coordinates by subtracting viewport offset
        Rectangle rect = map.getViewportBounds();
        int x = (int) point.getX() - rect.x;
        int y = (int) point.getY() - rect.y;
        
        // Draw pin marker (LARGER and MORE VISIBLE)
        int pinSize = 40;
        int innerSize = 28;
        
        // Outer circle (colored)
        g.setColor(color);
        g.fillOval(x - pinSize/2, y - pinSize, pinSize, pinSize);
        
        // Inner circle (white)
        g.setColor(Color.WHITE);
        g.fillOval(x - innerSize/2, y - pinSize + (pinSize - innerSize)/2, innerSize, innerSize);
        
        // Draw pointer
        int[] xPoints = {x, x - 12, x + 12};
        int[] yPoints = {y, y - 12, y - 12};
        g.setColor(color);
        g.fillPolygon(xPoints, yPoints, 3);
        
        // Draw label
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(label);
        
        // Background
        g.setColor(color);
        g.fillRoundRect(x - textWidth/2 - 6, y + 10, textWidth + 12, fm.getHeight() + 4, 8, 8);
        
        // Text
        g.setColor(Color.WHITE);
        g.drawString(label, x - textWidth/2, y + 10 + fm.getAscent() + 2);
    }
    
    /**
     * Draw road on map (with or without polyline)
     */
    private void drawRoad(Graphics2D g, JXMapViewer map, Road road) {
        // Determine color based on type and closure
        Color roadColor;
        boolean isClosed = activeClosures.containsKey(road.getRoadId()) || 
                          road.getRoadType() == Road.RoadType.CLOSED;
        
        if (isClosed) {
            roadColor = new Color(220, 50, 50); // MERAH = Tertutup
        } else if (road.getRoadType() == Road.RoadType.ONE_WAY) {
            roadColor = new Color(0, 100, 200); // BIRU = Satu Arah
        } else if (road.getRoadType() == Road.RoadType.TWO_WAY) {
            roadColor = new Color(0, 150, 0); // HIJAU = Dua Arah
        } else {
            roadColor = Color.BLACK; // HITAM = Normal
        }
        
        g.setColor(roadColor);
        g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Draw polyline if available
        if (road.getPolylinePoints() != null && !road.getPolylinePoints().isEmpty()) {
            List<GeoPosition> points = decodePolyline(road.getPolylinePoints());
            if (points != null && points.size() > 1) {
                for (int i = 0; i < points.size() - 1; i++) {
                    GeoPosition p1 = points.get(i);
                    GeoPosition p2 = points.get(i + 1);
                    
                    Point2D pt1 = map.getTileFactory().geoToPixel(p1, map.getZoom());
                    Point2D pt2 = map.getTileFactory().geoToPixel(p2, map.getZoom());
                    
                    g.drawLine((int)pt1.getX(), (int)pt1.getY(), (int)pt2.getX(), (int)pt2.getY());
                    
                    // Draw arrow for direction (every 3rd segment)
                    if (i % 3 == 0) {
                        drawArrow(g, (int)pt1.getX(), (int)pt1.getY(), (int)pt2.getX(), (int)pt2.getY(), 
                                 road.getRoadType() == Road.RoadType.ONE_WAY);
                    }
                }
                return;
            }
        }
        
        // Fallback: draw straight line
        GeoPosition start = new GeoPosition(road.getStartLat(), road.getStartLng());
        GeoPosition end = new GeoPosition(road.getEndLat(), road.getEndLng());
        
        Point2D p1 = map.getTileFactory().geoToPixel(start, map.getZoom());
        Point2D p2 = map.getTileFactory().geoToPixel(end, map.getZoom());
        
        g.drawLine((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY());
        
        // Draw arrow in middle
        drawArrow(g, (int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY(), 
                 road.getRoadType() == Road.RoadType.ONE_WAY);
    }
    
    /**
     * Draw directional arrow on road
     */
    private void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2, boolean oneWayOnly) {
        // Calculate angle
        double angle = Math.atan2(y2 - y1, x2 - x1);
        
        // Arrow at middle point
        int mx = (x1 + x2) / 2;
        int my = (y1 + y2) / 2;
        
        int arrowSize = 8;
        
        // Draw single arrow (for one-way)
        int ax1 = (int)(mx - arrowSize * Math.cos(angle - Math.PI / 6));
        int ay1 = (int)(my - arrowSize * Math.sin(angle - Math.PI / 6));
        int ax2 = (int)(mx - arrowSize * Math.cos(angle + Math.PI / 6));
        int ay2 = (int)(my - arrowSize * Math.sin(angle + Math.PI / 6));
        
        g.fillPolygon(new int[]{mx, ax1, ax2}, new int[]{my, ay1, ay2}, 3);
        
        // Draw reverse arrow for two-way
        if (!oneWayOnly) {
            int bx1 = (int)(mx + arrowSize * Math.cos(angle - Math.PI / 6));
            int by1 = (int)(my + arrowSize * Math.sin(angle - Math.PI / 6));
            int bx2 = (int)(mx + arrowSize * Math.cos(angle + Math.PI / 6));
            int by2 = (int)(my + arrowSize * Math.sin(angle + Math.PI / 6));
            
            g.fillPolygon(new int[]{mx, bx1, bx2}, new int[]{my, by1, by2}, 3);
        }
    }
    
    /**
     * Custom mouse listener yang handle drag marker DAN pan map
     */
    private class CustomMapMouseListener extends MouseAdapter {
        private final JXMapViewer viewer;
        private Point startDragPoint;
        private boolean isDraggingMap = false;
        
        public CustomMapMouseListener(JXMapViewer viewer) {
            this.viewer = viewer;
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            startDragPoint = e.getPoint();
            
            // Check if in add/edit mode dan klik dekat marker
            if (isAddingRoad || isEditingRoadPosition) {
                GeoPosition clickPos = viewer.convertPointToGeoPosition(e.getPoint());
                
                // Check if clicking near start or end marker
                if (startMarker != null && isNearMarker(clickPos, startMarker)) {
                    draggedMarker = startMarker;
                    System.out.println("🟢 START marker grabbed for dragging");
                    return; // Don't pan map
                } else if (endMarker != null && isNearMarker(clickPos, endMarker)) {
                    draggedMarker = endMarker;
                    System.out.println("🔴 END marker grabbed for dragging");
                    return; // Don't pan map
                }
            }
            
            // If not dragging marker, allow map pan
            isDraggingMap = true;
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            if (draggedMarker != null) {
                // Get final position
                Point point = e.getPoint();
                GeoPosition finalPos = viewer.convertPointToGeoPosition(point);
                
                // Save final position
                if (draggedMarker == startMarker) {
                    startMarker = finalPos;
                    System.out.println("✅ START marker final position: " + finalPos.getLatitude() + ", " + finalPos.getLongitude());
                } else if (draggedMarker == endMarker) {
                    endMarker = finalPos;
                    System.out.println("✅ END marker final position: " + finalPos.getLatitude() + ", " + finalPos.getLongitude());
                }
                
                // Force multiple repaints to ensure visual update
                viewer.repaint();
                viewer.revalidate();
                SwingUtilities.invokeLater(() -> viewer.repaint());
            }
            draggedMarker = null;
            isDraggingMap = false;
            startDragPoint = null;
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            // Only select road if not in add/edit mode
            if (e.getClickCount() == 1 && !isAddingRoad && !isEditingRoadPosition) {
                GeoPosition geoPos = viewer.convertPointToGeoPosition(e.getPoint());
                selectNearestRoad(geoPos.getLatitude(), geoPos.getLongitude());
            }
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {
            // Priority 1: Drag marker
            if (draggedMarker != null) {
                Point point = e.getPoint();
                GeoPosition newPos = viewer.convertPointToGeoPosition(point);
                
                // Update marker position
                if (draggedMarker == startMarker) {
                    startMarker = newPos;
                    System.out.println("🟢 START moved to: " + newPos.getLatitude() + ", " + newPos.getLongitude());
                } else if (draggedMarker == endMarker) {
                    endMarker = newPos;
                    System.out.println("🔴 END moved to: " + newPos.getLatitude() + ", " + newPos.getLongitude());
                }
                
                // CRITICAL: Force repaint by triggering map update
                viewer.setZoom(viewer.getZoom()); // Dummy zoom change to force repaint
                viewer.repaint();
                return; // Don't pan map when dragging marker
            }
            
            // Priority 2: Pan map (only if not dragging marker)
            if (isDraggingMap && startDragPoint != null) {
                Point currentPoint = e.getPoint();
                int dx = currentPoint.x - startDragPoint.x;
                int dy = currentPoint.y - startDragPoint.y;
                
                // Pan the map by adjusting center position
                Rectangle viewportBounds = viewer.getViewportBounds();
                int newX = viewportBounds.x - dx;
                int newY = viewportBounds.y - dy;
                
                // Calculate new center position
                Point2D centerPixel = new Point2D.Double(
                    newX + viewportBounds.width / 2.0,
                    newY + viewportBounds.height / 2.0
                );
                
                GeoPosition newCenter = viewer.getTileFactory().pixelToGeo(centerPixel, viewer.getZoom());
                viewer.setCenterPosition(newCenter);
                
                startDragPoint = currentPoint;
            }
        }
        
        @Override
        public void mouseMoved(MouseEvent e) {
            // Change cursor when hovering over marker
            if (isAddingRoad || isEditingRoadPosition) {
                GeoPosition hoverPos = viewer.convertPointToGeoPosition(e.getPoint());
                
                if ((startMarker != null && isNearMarker(hoverPos, startMarker)) ||
                    (endMarker != null && isNearMarker(hoverPos, endMarker))) {
                    viewer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    viewer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            } else {
                viewer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }
}
