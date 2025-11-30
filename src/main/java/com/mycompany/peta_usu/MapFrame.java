/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.peta_usu;

import com.mycompany.peta_usu.dao.BuildingDAO;
import com.mycompany.peta_usu.dao.MarkerDAO;
import com.mycompany.peta_usu.models.Building;
import com.mycompany.peta_usu.models.Marker;
import com.mycompany.peta_usu.ui.BuildingInfoDialog;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.*;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.WaypointPainter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.ArrayList;

/**
 * MapFrame - User Map View (Responsive)
 * Langsung accessible tanpa login
 * Integrated dengan database untuk building info
 * 
 * @author PETA_USU Team
 */
public class MapFrame extends javax.swing.JFrame {
    private JXMapViewer mapViewer;
    private String studentNim;
    private Set<CustomWaypoint> waypoints;
    private JDialog loadingDialog;
    private JComboBox<String> titikAwalCombo;
    private JComboBox<String> titikTujuanCombo;
    private JPanel infoPanel; // Left sidebar untuk info gedung
    private CustomWaypoint selectedWaypoint; // Waypoint yang diklik
    
    // Routing components
    private GeoPosition startPosition = null;
    private GeoPosition endPosition = null;
    private List<GeoPosition> routePath = new ArrayList<>();
    private boolean routeVisible = false;
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MapFrame.class.getName());
    
    // Database DAO
    private BuildingDAO buildingDAO;
    private MarkerDAO markerDAO;
    private List<Building> buildings;
    private List<Marker> markers;
    
    private static final Color PRIMARY_GREEN = new Color(0x388860);
    private static final Color LIGHT_GREEN = new Color(0x4CAF6E);
    private static final Font MAIN_FONT = new Font("Times New Roman", Font.PLAIN, 14);
    private static final Font LEGEND_FONT = new Font("Times New Roman", Font.BOLD, 16);
    
    /**
     * Creates new form MapFrame
     * Inisialisasi dengan DAO untuk load building data
     */
    public MapFrame(String nim) {
        this.studentNim = nim;
        this.waypoints = java.util.Collections.synchronizedSet(new HashSet<>());
        this.buildingDAO = new BuildingDAO();
        this.markerDAO = new MarkerDAO();
        this.buildings = new ArrayList<>();
        this.markers = new ArrayList<>();
        
        showLoadingDialog();
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                initComponents();
                setupMapUI();
                loadBuildingsFromDatabase();
                loadMarkersFromDatabase();
               
                Thread.sleep(1500);
                return null;
            }
            
            @Override
            protected void done() {
                hideLoadingDialog();
                updateWaypoints(); // Render icons after loading complete
                updateLocationComboBoxes(); // Populate combo boxes with loaded data
            }
        };
        worker.execute();
    }
    
    /**
     * Load buildings dari database
     */
    private void loadBuildingsFromDatabase() {
        try {
            buildings = buildingDAO.getAllBuildings();
            logger.info("Loaded " + buildings.size() + " buildings from database");
            
            // Add buildings sebagai waypoints
            for (Building building : buildings) {
                CustomWaypoint wp = new CustomWaypoint(
                    building.getBuildingName(),
                    new GeoPosition(building.getLatitude(), building.getLongitude()),
                    building.getBuildingType().getValue(),
                    null, // no custom icon for buildings
                    building.getDescription() != null ? building.getDescription() : ""
                );
                waypoints.add(wp);
            }
            
            // Update combo boxes after buildings loaded
            SwingUtilities.invokeLater(() -> {
                updateLocationComboBoxes();
            });
            
        } catch (Exception e) {
            logger.warning("Failed to load buildings: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load markers with custom icons from database (uploaded by admin)
     */
    private void loadMarkersFromDatabase() {
        try {
            markers = markerDAO.getAllMarkers();
            logger.info("Loaded " + markers.size() + " markers from database");
            
            // Add markers dengan custom icons
            for (Marker marker : markers) {
                CustomWaypoint wp = new CustomWaypoint(
                    marker.getMarkerName(),
                    new GeoPosition(marker.getLatitude(), marker.getLongitude()),
                    marker.getMarkerType(),
                    marker.getIconPath(), // custom icon path
                    marker.getDescription() != null ? marker.getDescription() : ""
                );
                waypoints.add(wp);
            }
            
            // Update combo boxes after markers loaded
            SwingUtilities.invokeLater(() -> {
                List<String> lokasiUSU = new ArrayList<>();
                synchronized (waypoints) {
                    for (CustomWaypoint wp : waypoints) {
                        lokasiUSU.add(wp.getName());
                    }
                }
                java.util.Collections.sort(lokasiUSU);
                
                if (titikAwalCombo != null) {
                    titikAwalCombo.removeAllItems();
                    for (String lokasi : lokasiUSU) {
                        titikAwalCombo.addItem(lokasi);
                    }
                }
                
                if (titikTujuanCombo != null) {
                    titikTujuanCombo.removeAllItems();
                    for (String lokasi : lokasiUSU) {
                        titikTujuanCombo.addItem(lokasi);
                    }
                }
                
                logger.info("Combo boxes updated with " + lokasiUSU.size() + " locations");
            });
            
        } catch (Exception e) {
            logger.warning("Failed to load markers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
     private void showLoadingDialog() {
        loadingDialog = new JDialog(this, "Memuat Peta", true);
        loadingDialog.setUndecorated(true);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_GREEN, 3),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        panel.setBackground(Color.WHITE);
        
        JLabel iconLabel = new JLabel("🗺️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel textLabel = new JLabel("Memuat Peta USU...");
        textLabel.setFont(new Font("Times New Roman", Font.BOLD, 16));
        textLabel.setForeground(PRIMARY_GREEN);
        textLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(250, 10));
        progressBar.setForeground(PRIMARY_GREEN);
        
        panel.add(iconLabel, BorderLayout.NORTH);
        panel.add(textLabel, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);
        
        loadingDialog.add(panel);
        loadingDialog.pack();
        loadingDialog.setLocationRelativeTo(null);
        
        new Thread(() -> loadingDialog.setVisible(true)).start();
    }
    
    private void hideLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog.setVisible(false);
            loadingDialog.dispose();
        }
    }
    
    private void setupMapUI() {
        setTitle("Peta Kampus USU");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Make responsive
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                // Auto-adjust layout saat window di-resize
                revalidate();
                repaint();
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Left info sidebar
        infoPanel = createInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.WEST);

        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(Color.WHITE);
        
        JPanel routePanel = createRouteSelectionPanel();
        centerContainer.add(routePanel, BorderLayout.NORTH);
        
        JPanel mapPanel = createMapPanel();
        centerContainer.add(mapPanel, BorderLayout.CENTER);
        
        mainPanel.add(centerContainer, BorderLayout.CENTER);

        setContentPane(mainPanel);
        
        // Maximize window for better responsiveness
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_GREEN);
        headerPanel.setPreferredSize(new Dimension(0, 50));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));

        JLabel titleLabel = new JLabel("Selamat Datang di Peta USU");
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 20)); 
        titleLabel.setForeground(Color.WHITE);

        JLabel nimLabel = new JLabel("NIM: " + studentNim);
        nimLabel.setFont(new Font("Times New Roman", Font.PLAIN, 12)); 
        nimLabel.setForeground(Color.WHITE);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(titleLabel, BorderLayout.CENTER);
        textPanel.add(nimLabel, BorderLayout.SOUTH);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(PRIMARY_GREEN);
        logoutButton.setFont(new Font("Times New Roman", Font.BOLD, 11)); 
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        logoutButton.setPreferredSize(new Dimension(70, 22));
        logoutButton.setMargin(new Insets(2, 8, 2, 8));
        logoutButton.setBorder(BorderFactory.createLineBorder(PRIMARY_GREEN, 1, true));
        logoutButton.setContentAreaFilled(false);
        logoutButton.setOpaque(true);
        logoutButton.addActionListener(e -> logout());

        headerPanel.add(textPanel, BorderLayout.CENTER);
        headerPanel.add(logoutButton, BorderLayout.EAST);
        return headerPanel;
    }
    
    private JComboBox<String> createSearchableComboBox(List<String> allItems) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (String s : allItems) model.addElement(s);

        JComboBox<String> combo = new JComboBox<>(model);
        combo.setEditable(true);
        combo.setMaximumRowCount(10);
        combo.setFont(MAIN_FONT);
        combo.setBackground(Color.WHITE);

        JTextField editor = (JTextField) combo.getEditor().getEditorComponent();
        List<String> master = new ArrayList<>(allItems);
        AtomicBoolean updating = new AtomicBoolean(false);

        editor.getDocument().addDocumentListener(new DocumentListener() {
            private void doFilter() {
                if (updating.get()) return;

                SwingUtilities.invokeLater(() -> {
                    updating.set(true);
                    try {
                        String text = editor.getText(); 
                        int caretPos = editor.getCaretPosition();

                        model.removeAllElements();
                        String searchText = text.toLowerCase(); 

                        for (String s : master) {
                            if (searchText.isEmpty() || s.toLowerCase().contains(searchText)) {
                                model.addElement(s);
                            }
                        }

                        combo.setSelectedItem(null);
                        editor.setText(text);
                        editor.setCaretPosition(Math.min(caretPos, text.length()));

                        if (model.getSize() > 0 && !text.isEmpty()) {
                            combo.showPopup();
                        } else {
                            combo.hidePopup();
                        }
                    } finally {
                        updating.set(false);
                    }
                });
            }

            @Override public void insertUpdate(DocumentEvent e) { doFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { doFilter(); }
            @Override public void changedUpdate(DocumentEvent e) {}
        });

        combo.addActionListener(e -> {
            if (updating.get()) return;

            Object sel = combo.getSelectedItem();
            if (sel != null) {
                String selectedText = sel.toString();
                String currentText = editor.getText();

                if (!selectedText.equals(currentText)) {
                    updating.set(true);
                    try {
                        editor.setText(selectedText);
                        editor.setCaretPosition(selectedText.length());
                        combo.hidePopup();
                    } finally {
                        updating.set(false);
                    }
                }
            }
        });

        editor.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (!updating.get()) {
                        editor.selectAll(); 
                        combo.showPopup();
                    }
                });
            }
        });

        editor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    if (combo.isPopupVisible() && model.getSize() > 0) {
                        updating.set(true);
                        try {
                            combo.setSelectedIndex(0);
                            Object sel = combo.getSelectedItem();
                            if (sel != null) {
                                editor.setText(sel.toString());
                            }
                            combo.hidePopup();
                        } finally {
                            updating.set(false);
                        }
                    }
                } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    combo.hidePopup();
                }
            }
        });

        return combo;
    }
     
    private JPanel createRouteSelectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        panel.setBackground(new Color(0xF5F5F5));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_GREEN),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JLabel labelAwal = new JLabel("Titik Awal:");
        labelAwal.setFont(new Font("Times New Roman", Font.BOLD, 14));
        labelAwal.setForeground(PRIMARY_GREEN);

        JLabel iconAwal = new JLabel("📍");
        iconAwal.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        JLabel labelTujuan = new JLabel("Titik Tujuan:");
        labelTujuan.setFont(new Font("Times New Roman", Font.BOLD, 14));
        labelTujuan.setForeground(PRIMARY_GREEN);

        JLabel iconTujuan = new JLabel("🎯");
        iconTujuan.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        // Populate location list from database (buildings + markers)
        List<String> lokasiUSU = new ArrayList<>();
        synchronized (waypoints) {
            for (CustomWaypoint wp : waypoints) {
                lokasiUSU.add(wp.getName());
            }
        }
        java.util.Collections.sort(lokasiUSU); // Sort alphabetically

        titikAwalCombo = createSearchableComboBox(lokasiUSU);
        titikAwalCombo.setPreferredSize(new Dimension(250, 30));

        titikTujuanCombo = createSearchableComboBox(lokasiUSU);
        titikTujuanCombo.setPreferredSize(new Dimension(250, 30));

        JButton cariRuteButton = new JButton("Cari Rute");
        cariRuteButton.setFont(new Font("Times New Roman", Font.BOLD, 13));
        cariRuteButton.setBackground(PRIMARY_GREEN);
        cariRuteButton.setForeground(Color.WHITE);
        cariRuteButton.setPreferredSize(new Dimension(100, 30));
        cariRuteButton.setFocusPainted(false);
        cariRuteButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        cariRuteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        cariRuteButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { cariRuteButton.setBackground(LIGHT_GREEN); }
            @Override public void mouseExited(MouseEvent e) { cariRuteButton.setBackground(PRIMARY_GREEN); }
        });

        cariRuteButton.addActionListener(e -> {
            String awal = (String) titikAwalCombo.getSelectedItem();
            String tujuan = (String) titikTujuanCombo.getSelectedItem();
            if (awal == null || tujuan == null || awal.equals(tujuan)) {
                JOptionPane.showMessageDialog(this,
                    "Pilih titik awal dan titik tujuan yang berbeda!",
                    "Peringatan",
                    JOptionPane.WARNING_MESSAGE);
            } else {
                calculateAndShowRoute(awal, tujuan);
            }
        });

        panel.add(iconAwal);
        panel.add(labelAwal);
        panel.add(titikAwalCombo);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(iconTujuan);
        panel.add(labelTujuan);
        panel.add(titikTujuanCombo);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(cariRuteButton);

        return panel;
    }

     
     private JPanel createMapPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        panel.setBackground(Color.WHITE);

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

        GeoPosition usu = new GeoPosition(3.5688, 98.6618);
        mapViewer.setZoom(3);
        mapViewer.setAddressLocation(usu);

        MouseAdapter pan = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(pan);
        mapViewer.addMouseMotionListener(pan);
        
        final int MIN_OUT_ZOOM = 1;
        final int MIN_IN_ZOOM  = 0; 

        mapViewer.addMouseWheelListener(e -> {
            int z = mapViewer.getZoom();

            if (e.getWheelRotation() > 0) {
                if (z != MIN_OUT_ZOOM) {
                    mapViewer.setZoom(MIN_OUT_ZOOM);
                }
            } else if (e.getWheelRotation() < 0) {
                if (z > MIN_IN_ZOOM) {
                    mapViewer.setZoom(z - 1);
                } else {
                    mapViewer.setZoom(MIN_IN_ZOOM);
                }
            }
        });

        panel.add(mapViewer, BorderLayout.CENTER);
        logger.info("Map panel created with Google Maps");
        return panel;
    }
     
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(250, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 2, 0, 0, PRIMARY_GREEN),
            BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));

        JLabel legendLabel = new JLabel("Legenda:");
        legendLabel.setFont(LEGEND_FONT);
        legendLabel.setForeground(PRIMARY_GREEN);
        panel.add(legendLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBackground(Color.WHITE);

        try {
            File file = new File("resources/kampus_usu.geojson");
            if (file.exists()) {
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONObject geoJson = new JSONObject(content);
                JSONArray features = geoJson.getJSONArray("features");

                java.util.Map<String, String> legendMap = new java.util.LinkedHashMap<>();

                for (int i = 0; i < features.length(); i++) {
                    JSONObject props = features.getJSONObject(i).getJSONObject("properties");
                    String type = props.optString("type", "Lainnya");
                    String icon = props.optString("icon", "📍");
                    legendMap.putIfAbsent(type, icon);
                }

                for (var entry : legendMap.entrySet()) {
                    addLegendItem(legendPanel, entry.getValue(), entry.getKey(), PRIMARY_GREEN);
                }
            }
        } catch (Exception e) {
            logger.warning("Gagal memuat legenda dinamis: " + e.getMessage());
        }

        panel.add(legendPanel);
        panel.add(Box.createVerticalGlue());

        JTextArea info = new JTextArea("Tips:\n• Klik icon untuk detail\n• Scroll untuk zoom\n• Drag untuk geser peta");
        info.setFont(new Font("Times New Roman", Font.PLAIN, 11));
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setBackground(new Color(248, 249, 250));
        info.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_GREEN, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        panel.add(info);
        return panel;
    }
    
    /**
     * Create left info panel untuk menampilkan info gedung saat diklik
     */
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 2, PRIMARY_GREEN),
            BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));
        
        JLabel titleLabel = new JLabel("📍 Info Lokasi");
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_GREEN);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JTextArea infoText = new JTextArea("Klik icon di peta untuk melihat informasi detail lokasi.");
        infoText.setFont(MAIN_FONT);
        infoText.setEditable(false);
        infoText.setLineWrap(true);
        infoText.setWrapStyleWord(true);
        infoText.setBackground(Color.WHITE);
        infoText.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        panel.add(infoText, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void showWaypointInfo(CustomWaypoint waypoint) {
        // Update info panel with waypoint details
        Component[] components = infoPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JTextArea) {
                JTextArea textArea = (JTextArea) comp;
                StringBuilder info = new StringBuilder();
                info.append("🏢 Nama: ").append(waypoint.getName()).append("\n\n");
                info.append("📍 Tipe: ").append(waypoint.getType()).append("\n\n");
                
                // Get position
                GeoPosition pos = waypoint.getPosition();
                info.append("🌍 Koordinat:\n");
                info.append(String.format("Lat: %.6f\nLon: %.6f\n\n", pos.getLatitude(), pos.getLongitude()));
                
                if (waypoint.getDescription() != null && !waypoint.getDescription().isEmpty()) {
                    info.append("ℹ️ Deskripsi:\n").append(waypoint.getDescription()).append("\n\n");
                }
                
                info.append("💡 Tip: Pilih sebagai titik tujuan untuk melihat rute!");
                
                textArea.setText(info.toString());
                selectedWaypoint = waypoint;
                break;
            }
        }
    }
    
    private void addLegendItem(JPanel panel, String icon, String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        item.setBackground(Color.WHITE);
        item.setMaximumSize(new Dimension(230, 30));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Times New Roman", Font.PLAIN, 13));

        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(12, 12));
        colorBox.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

        item.add(colorBox);
        item.add(iconLabel);
        item.add(textLabel);
        panel.add(item);
    }
    
    /**
     * Calculate route from start to end location and display on map
     * Using A* pathfinding algorithm
     */
    private void calculateAndShowRoute(String startName, String endName) {
        // Find start and end positions from waypoints
        CustomWaypoint start = null;
        CustomWaypoint end = null;
        
        synchronized (waypoints) {
            for (CustomWaypoint wp : waypoints) {
                if (wp.getName().equalsIgnoreCase(startName)) {
                    start = wp;
                }
                if (wp.getName().equalsIgnoreCase(endName)) {
                    end = wp;
                }
            }
        }
        
        if (start == null || end == null) {
            JOptionPane.showMessageDialog(this,
                "Lokasi tidak ditemukan di map!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Set start and end positions
        startPosition = start.getPosition();
        endPosition = end.getPosition();
        
        // Use PathfindingService to find shortest path using A* algorithm
        com.mycompany.peta_usu.services.PathfindingService pathfinder = 
            new com.mycompany.peta_usu.services.PathfindingService();
        
        com.mycompany.peta_usu.services.PathfindingService.RouteResult route = 
            pathfinder.findShortestPath(
                startPosition.getLatitude(),
                startPosition.getLongitude(),
                endPosition.getLatitude(),
                endPosition.getLongitude()
            );
        
        // Convert PathfindingService.LatLng to GeoPosition
        routePath.clear();
        if (route.polyline != null && !route.polyline.isEmpty()) {
            for (com.mycompany.peta_usu.services.PathfindingService.LatLng point : route.polyline) {
                routePath.add(new GeoPosition(point.lat, point.lng));
            }
            routeVisible = true;
        } else {
            // Fallback to straight line if no path found
            routePath.add(startPosition);
            routePath.add(endPosition);
            routeVisible = true;
        }
        
        // Update waypoints to re-render with route
        updateWaypoints();
        
        // Center map on route
        double centerLat = (startPosition.getLatitude() + endPosition.getLatitude()) / 2;
        double centerLon = (startPosition.getLongitude() + endPosition.getLongitude()) / 2;
        mapViewer.setAddressLocation(new GeoPosition(centerLat, centerLon));
        
        // Show route info dengan data dari A* algorithm
        String pathInfo = route.roadsUsed.isEmpty() ? 
            "Rute langsung (tidak ada jalan)" : 
            String.format("Melalui %d jalan", route.roadsUsed.size());
        
        JOptionPane.showMessageDialog(this,
            String.format(
                "<html><body style='font-family: Times New Roman; padding: 10px;'>" +
                "<h3 style='color: #388860;'>📍 Informasi Rute Tercepat (A* Algorithm)</h3>" +
                "<p><b>Dari:</b> %s</p>" +
                "<p><b>Ke:</b> %s</p>" +
                "<p><b>Jarak:</b> %.2f km</p>" +
                "<p><b>Estimasi Waktu:</b> %d menit (jalan kaki)</p>" +
                "<p><b>Rute:</b> %s</p>" +
                "<p style='color: #0066cc;'><i>Menggunakan pathfinding A* untuk rute tercepat</i></p>" +
                "</body></html>",
                startName, endName, route.totalDistanceKm, route.estimatedMinutes, pathInfo
            ),
            "Info Rute Tercepat",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Calculate distance between two GeoPositions using Haversine formula
     * @return distance in kilometers
     */
    private double calculateDistance(GeoPosition pos1, GeoPosition pos2) {
        final double R = 6371; // Earth radius in km
        double lat1 = Math.toRadians(pos1.getLatitude());
        double lat2 = Math.toRadians(pos2.getLatitude());
        double dLat = Math.toRadians(pos2.getLatitude() - pos1.getLatitude());
        double dLon = Math.toRadians(pos2.getLongitude() - pos1.getLongitude());
        
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        
        return R * c;
    }
    
    /**
     * Update combo boxes with loaded waypoints
     */
    private void updateLocationComboBoxes() {
        List<String> lokasiUSU = new ArrayList<>();
        synchronized (waypoints) {
            for (CustomWaypoint wp : waypoints) {
                lokasiUSU.add(wp.getName());
            }
        }
        java.util.Collections.sort(lokasiUSU); // Sort alphabetically
        
        // Update titik awal combo
        if (titikAwalCombo != null) {
            titikAwalCombo.removeAllItems();
            for (String lokasi : lokasiUSU) {
                titikAwalCombo.addItem(lokasi);
            }
        }
        
        // Update titik tujuan combo
        if (titikTujuanCombo != null) {
            titikTujuanCombo.removeAllItems();
            for (String lokasi : lokasiUSU) {
                titikTujuanCombo.addItem(lokasi);
            }
        }
        
        logger.info("Combo boxes updated with " + lokasiUSU.size() + " locations");
    }

    private void addMarker(double lat, double lon, String name, String type, String description, String icon) {
        CustomWaypoint waypoint = new CustomWaypoint(name, type, description, icon, new GeoPosition(lat, lon), null);
        waypoints.add(waypoint);
        logger.info(String.format("Added marker: %s (%s) at (%.6f, %.6f)", name, type, lat, lon));
    }
    
    private void updateWaypoints() {
        WaypointPainter<CustomWaypoint> waypointPainter = new WaypointPainter<CustomWaypoint>() {
            @Override
            protected void doPaint(Graphics2D g, JXMapViewer map, int width, int height) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw route polyline if routing is active
                if (routeVisible && !routePath.isEmpty()) {
                    g.setColor(new Color(66, 133, 244, 200)); // Google Maps blue
                    g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    
                    for (int i = 0; i < routePath.size() - 1; i++) {
                        Point2D p1 = map.convertGeoPositionToPoint(routePath.get(i));
                        Point2D p2 = map.convertGeoPositionToPoint(routePath.get(i + 1));
                        g.drawLine((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY());
                    }
                    
                    // Draw start marker (green)
                    if (startPosition != null) {
                        Point2D startPt = map.convertGeoPositionToPoint(startPosition);
                        g.setColor(new Color(52, 168, 83)); // Google Maps green
                        g.fillOval((int)startPt.getX() - 8, (int)startPt.getY() - 8, 16, 16);
                        g.setColor(Color.WHITE);
                        g.fillOval((int)startPt.getX() - 4, (int)startPt.getY() - 4, 8, 8);
                    }
                    
                    // Draw end marker (red)
                    if (endPosition != null) {
                        Point2D endPt = map.convertGeoPositionToPoint(endPosition);
                        g.setColor(new Color(234, 67, 53)); // Google Maps red
                        g.fillOval((int)endPt.getX() - 8, (int)endPt.getY() - 8, 16, 16);
                        g.setColor(Color.WHITE);
                        g.fillOval((int)endPt.getX() - 4, (int)endPt.getY() - 4, 8, 8);
                    }
                }

                // Calculate icon size based on zoom level
                int zoom = map.getZoom();
                double zoomFactor = Math.max(0.2, Math.min(1.0, (16 - zoom) / 10.0)); // 0.2 to 1.0
                int iconSize = (int)(28 * zoomFactor);
                int iconHalf = iconSize / 2;
                int fontSize = Math.max(7, (int)(9 * zoomFactor));

                // Synchronized iteration to prevent ConcurrentModificationException
                synchronized (waypoints) {
                    for (CustomWaypoint wp : waypoints) {
                        Point2D point = mapViewer.convertGeoPositionToPoint(wp.getPosition());
                        int x = (int) point.getX();
                        int y = (int) point.getY();

                        // If waypoint has custom icon path, render uploaded icon
                        if (wp.getIconPath() != null && !wp.getIconPath().isEmpty()) {
                            try {
                                File iconFile = new File(wp.getIconPath());
                                if (iconFile.exists()) {
                                    BufferedImage icon = ImageIO.read(iconFile);
                                    // Render icon centered at position with zoom-scaled size
                                    g.drawImage(icon, x - iconHalf, y - iconHalf, iconSize, iconSize, null);
                                } else {
                                    // Fallback to default pin if file not found
                                    g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, (int)(18 * zoomFactor)));
                                    g.drawString("📍", x - (int)(8 * zoomFactor), y + (int)(6 * zoomFactor));
                                }
                            } catch (Exception e) {
                                // Fallback to default pin on error
                                g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, (int)(18 * zoomFactor)));
                                g.drawString("📍", x - (int)(8 * zoomFactor), y + (int)(6 * zoomFactor));
                            }
                        } else {
                            // Default emoji icon for buildings and GeoJSON markers
                            g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, (int)(18 * zoomFactor)));
                            String iconEmoji = wp.getIcon() != null && !wp.getIcon().isEmpty() ? wp.getIcon() : "📍";
                            g.drawString(iconEmoji, x - (int)(8 * zoomFactor), y + (int)(6 * zoomFactor));
                        }

                        // Draw label below icon with scaled font
                        g.setFont(new Font("Times New Roman", Font.BOLD, fontSize));
                        FontMetrics fm = g.getFontMetrics();
                        int labelWidth = fm.stringWidth(wp.getName());

                        g.setColor(new Color(255, 255, 255, 230));
                        g.fillRoundRect(x - labelWidth/2 - 4, y + 8, labelWidth + 8, 14, 5, 5);
                        g.setColor(Color.BLACK);
                        g.drawString(wp.getName(), x - labelWidth/2, y + 19);
                    }
                }
            }
        };

        waypointPainter.setWaypoints(waypoints);

        // Set painter tanpa area polygon
        mapViewer.setOverlayPainter(waypointPainter);

        for (MouseListener ml : mapViewer.getMouseListeners()) {
            if (ml.getClass().isAnonymousClass()) {
                mapViewer.removeMouseListener(ml);
            }
        }

        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    Point point = e.getPoint();
                    // Synchronized iteration to prevent ConcurrentModificationException
                    synchronized (waypoints) {
                        for (CustomWaypoint wp : waypoints) {
                            Point2D waypointPoint = mapViewer.convertGeoPositionToPoint(wp.getPosition());
                            if (point.distance(waypointPoint) < 15) {
                                showWaypointInfo(wp);
                                break;
                            }
                        }
                    }
                }
            }
        });

        logger.info("Waypoints updated and painted on map");
    }

    private Color getMarkerColor(String type) {
        switch (type.toLowerCase()) {
            case "fakultas": return new Color(220, 53, 69);
            case "perpustakaan": return new Color(0, 123, 255);
            case "musholla": return new Color(40, 167, 69);
            case "kesehatan": return new Color(255, 193, 7);
            case "kantin": return new Color(108, 117, 125);
            case "olahraga": return new Color(23, 162, 184);
            default: return Color.RED;
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin kembali?",
                "Konfirmasi",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }
    
    private static class CustomWaypoint extends DefaultWaypoint {
        private final String name;
        private final String type;
        private final String description;
        private final String icon;
        private final String iconPath;

        // Constructor dengan 5 parameters (dengan description)
        public CustomWaypoint(String name, GeoPosition coord, String type, String iconPath, String description) {
            super(coord);
            this.name = name;
            this.type = type;
            this.description = description != null ? description : "";
            this.icon = "";
            this.iconPath = iconPath;
        }

        // Constructor lengkap untuk GeoJSON dengan emoji icons (jika diperlukan)
        public CustomWaypoint(String name, String type, String description, String icon, GeoPosition coord, String iconPath) {
            super(coord);
            this.name = name;
            this.type = type;
            this.description = description != null ? description : "";
            this.icon = icon;
            this.iconPath = iconPath;
        }

        public String getName() { return name; }
        public String getType() { return type; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
        public String getIconPath() { return iconPath; }
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                javax.swing.UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
    } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
        logger.log(java.util.logging.Level.SEVERE, null, ex);
    }
        java.awt.EventQueue.invokeLater(() -> new MapFrame("2205181001").setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
