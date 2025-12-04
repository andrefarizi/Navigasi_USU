/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.peta_usu;

import com.mycompany.peta_usu.dao.MarkerDAO;
import com.mycompany.peta_usu.dao.RoadDAO;
import com.mycompany.peta_usu.dao.RoadClosureDAO;
import com.mycompany.peta_usu.dao.ReportDAO;
import com.mycompany.peta_usu.models.Marker;
import com.mycompany.peta_usu.models.Road;
import com.mycompany.peta_usu.models.RoadClosure;
import com.mycompany.peta_usu.models.Report;
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
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    // User position marker
    private GeoPosition userPosition;
    private boolean isDraggingUserMarker = false;
    private static final String USER_MARKER_NAME = "📍 Posisi Saya";
    
    // Pan listener reference to preserve it when removing other listeners
    private PanMouseInputListener panListener;
    
    // Responsive layout components
    private JPanel mainPanel;
    private JPanel centerContainer;
    private boolean isMobileLayout = false;
    private static final int MOBILE_BREAKPOINT = 768; // Screen width threshold for mobile layout
    
    // Routing components
    private GeoPosition startPosition = null;
    private GeoPosition endPosition = null;
    private List<GeoPosition> routePath = new ArrayList<>();
    private boolean routeVisible = false;
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MapFrame.class.getName());
    
    // Database DAO
    private MarkerDAO markerDAO;
    private RoadDAO roadDAO;
    private RoadClosureDAO roadClosureDAO;
    private List<Marker> markers;
    private List<Road> roads;
    private List<RoadClosure> activeClosures;
    private Map<Integer, RoadClosure> closureMap;
    private JPanel legendPanel; // Panel legenda yang akan di-refresh
    
    private static final Color PRIMARY_GREEN = new Color(0x388860);
    private static final Color LIGHT_GREEN = new Color(0x4CAF6E);
    private static final Font MAIN_FONT = new Font("Times New Roman", Font.PLAIN, 14);
    private static final Font LEGEND_FONT = new Font("Times New Roman", Font.BOLD, 16);
    
    /**
     * Creates new form MapFrame
     * Inisialisasi dengan DAO untuk load data
     */
    public MapFrame(String nim) {
        this.studentNim = nim;
        this.waypoints = java.util.Collections.synchronizedSet(new HashSet<>());
        this.markerDAO = new MarkerDAO();
        this.roadDAO = new RoadDAO();
        this.roadClosureDAO = new RoadClosureDAO();
        this.markers = new ArrayList<>();
        this.roads = new ArrayList<>();
        this.activeClosures = new ArrayList<>();
        this.closureMap = new HashMap<>();
        
        // Initialize user position at USU center
        this.userPosition = new GeoPosition(3.5688, 98.6618);
        
        // Register this MapFrame for auto-refresh when admin adds markers
        com.mycompany.peta_usu.utils.MapRefreshUtil.registerMapFrame(this);
        
        showLoadingDialog();
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                initComponents();
                setupMapUI();
                loadMarkersFromDatabase();
                loadRoadsFromDatabase();
               
                Thread.sleep(1500);
                return null;
            }
            
            @Override
            protected void done() {
                hideLoadingDialog();
                updateWaypoints(); // Render icons after loading complete
                updateLocationComboBoxes(); // Populate combo boxes with loaded data
                refreshLegend(); // Refresh legend setelah markers dimuat
            }
        };
        worker.execute();
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
            
        } catch (Exception e) {
            logger.warning("Failed to load markers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load roads and closures from database untuk ditampilkan di map
     */
    private void loadRoadsFromDatabase() {
        try {
            roads = roadDAO.getAllRoads();
            activeClosures = roadClosureDAO.getActiveClosures();
            
            logger.info("Loaded " + roads.size() + " roads from database");
            logger.info("Loaded " + activeClosures.size() + " active closures");
            
            // Build closure map for fast lookup
            closureMap.clear();
            if (activeClosures != null) {
                for (RoadClosure closure : activeClosures) {
                    closureMap.put(closure.getRoadId(), closure);
                }
            }
            
        } catch (Exception e) {
            logger.warning("Failed to load roads: " + e.getMessage());
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

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Left info sidebar
        infoPanel = createInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.WEST);

        centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(Color.WHITE);
        
        JPanel routePanel = createRouteSelectionPanel();
        centerContainer.add(routePanel, BorderLayout.NORTH);
        
        JPanel mapPanel = createMapPanel();
        centerContainer.add(mapPanel, BorderLayout.CENTER);
        
        mainPanel.add(centerContainer, BorderLayout.CENTER);
        
        // Add control panel with report button on the right
        JPanel controlPanel = createControlPanel();
        JScrollPane controlScrollPane = new JScrollPane(controlPanel);
        controlScrollPane.setBorder(null);
        controlScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        controlScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainPanel.add(controlScrollPane, BorderLayout.EAST);

        setContentPane(mainPanel);
        
        // Add ComponentListener for responsive layout
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateLayoutForScreenSize();
            }
        });
        
        // Maximize window for better responsiveness
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Initial layout check
        SwingUtilities.invokeLater(() -> updateLayoutForScreenSize());
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_GREEN);
        headerPanel.setPreferredSize(new Dimension(0, 50));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));

        JLabel titleLabel = new JLabel("Selamat Datang di Peta USU");
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 20)); 
        titleLabel.setForeground(Color.WHITE);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(titleLabel, BorderLayout.CENTER);

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
        // Use FlowLayout that wraps automatically on small screens
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
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

        // Populate location list from database (buildings + markers + user position)
        List<String> lokasiUSU = new ArrayList<>();
        lokasiUSU.add(USER_MARKER_NAME); // Add user position first
        synchronized (waypoints) {
            for (CustomWaypoint wp : waypoints) {
                lokasiUSU.add(wp.getName());
            }
        }
        // Don't sort - keep user position first

        titikAwalCombo = createSearchableComboBox(lokasiUSU);
        titikAwalCombo.setPreferredSize(new Dimension(200, 30)); // Smaller for mobile
        titikAwalCombo.setMinimumSize(new Dimension(150, 30));

        titikTujuanCombo = createSearchableComboBox(lokasiUSU);
        titikTujuanCombo.setPreferredSize(new Dimension(200, 30)); // Smaller for mobile
        titikTujuanCombo.setMinimumSize(new Dimension(150, 30));

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

        // Custom pan listener that respects isDraggingUserMarker flag
        panListener = new PanMouseInputListener(mapViewer) {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isDraggingUserMarker) {
                    super.mousePressed(e);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!isDraggingUserMarker) {
                    super.mouseReleased(e);
                }
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isDraggingUserMarker) {
                    super.mouseDragged(e);
                }
            }
        };
        mapViewer.addMouseListener(panListener);
        mapViewer.addMouseMotionListener(panListener);
        
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

        legendPanel = new JPanel(); // Gunakan field class, bukan variabel lokal
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBackground(Color.WHITE);

        // Legenda akan di-load oleh refreshLegend() setelah markers dimuat
        // Tampilkan loading placeholder
        addLegendItem(legendPanel, "⏳", "Memuat...", PRIMARY_GREEN, null);

        panel.add(legendPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Tombol Lapor Kondisi Jalan
        JButton btnReport = new JButton("📢 Lapor Kondisi Jalan");
        btnReport.setFont(new Font("Times New Roman", Font.BOLD, 12));
        btnReport.setBackground(new Color(255, 152, 0));
        btnReport.setForeground(Color.WHITE);
        btnReport.setFocusPainted(false);
        btnReport.setBorderPainted(false);
        btnReport.setOpaque(true);
        btnReport.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnReport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReport.addActionListener(e -> showReportDialog());
        panel.add(btnReport);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Tombol Refresh/Reset Rute
        JButton btnRefresh = new JButton("🔄 Reset Rute");
        btnRefresh.setFont(new Font("Times New Roman", Font.BOLD, 12));
        btnRefresh.setBackground(new Color(76, 175, 80)); // Green
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setOpaque(true);
        btnRefresh.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setToolTipText("Reset titik awal dan tujuan, hapus rute dari peta");
        
        btnRefresh.addMouseListener(new MouseAdapter() {
            @Override 
            public void mouseEntered(MouseEvent e) { 
                btnRefresh.setBackground(new Color(102, 187, 106)); 
            }
            @Override 
            public void mouseExited(MouseEvent e) { 
                btnRefresh.setBackground(new Color(76, 175, 80)); 
            }
        });
        
        btnRefresh.addActionListener(e -> {
            // Close current window
            this.dispose();
            
            // Create and show new MapFrame (full refresh)
            SwingUtilities.invokeLater(() -> {
                MapFrame newFrame = new MapFrame("user");
                newFrame.setVisible(true);
            });
            
            logger.info("MapFrame refreshed - new instance created");
        });
        
        panel.add(btnRefresh);
        
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
        addLegendItem(panel, icon, text, color, null);
    }
    
    /**
     * Add legend item dengan support untuk gambar icon
     * @param panel Panel target
     * @param icon Icon emoji (fallback)
     * @param text Nama/label
     * @param color Warna (untuk fallback)
     * @param iconPath Path ke file icon gambar
     */
    private void addLegendItem(JPanel panel, String icon, String text, Color color, String iconPath) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        item.setBackground(Color.WHITE);
        item.setMaximumSize(new Dimension(250, 40));
        item.setPreferredSize(new Dimension(250, 35));

        // Coba load gambar icon jika iconPath tersedia
        if (iconPath != null && !iconPath.isEmpty()) {
            try {
                File iconFile = new File(iconPath);
                if (iconFile.exists()) {
                    BufferedImage iconImg = ImageIO.read(iconFile);
                    // Resize ke ukuran yang sesuai untuk legenda (24x24)
                    Image scaledIcon = iconImg.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                    JLabel iconLabel = new JLabel(new ImageIcon(scaledIcon));
                    iconLabel.setPreferredSize(new Dimension(24, 24));
                    item.add(iconLabel);
                    
                    // Text dengan font yang jelas dan NAMA LENGKAP
                    JLabel textLabel = new JLabel(text);
                    textLabel.setFont(new Font("Arial", Font.BOLD, 14));
                    textLabel.setForeground(new Color(51, 51, 51));
                    item.add(textLabel);
                    
                    panel.add(item);
                    return; // Berhasil tampilkan gambar icon
                }
            } catch (Exception e) {
                logger.warning("Gagal load icon untuk legenda: " + iconPath + " - " + e.getMessage());
                // Fall through ke default rendering
            }
        }
        
        // Fallback: Color box sebagai icon visual (jika gambar tidak tersedia)
        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(16, 16));
        colorBox.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

        // Text dengan font yang jelas dan NAMA LENGKAP
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Arial", Font.BOLD, 14));
        textLabel.setForeground(new Color(51, 51, 51));

        item.add(colorBox);
        item.add(textLabel);
        panel.add(item);
    }
    
    /**
     * Refresh legenda panel dengan data marker dari database
     */
    private void refreshLegend() {
        if (legendPanel == null) return;
        
        SwingUtilities.invokeLater(() -> {
            legendPanel.removeAll();
            
            try {
                // Map untuk group by icon path (bukan marker type)
                java.util.Map<String, Marker> uniqueMarkers = new java.util.LinkedHashMap<>();
                
                // Group markers by icon path untuk avoid duplicates
                for (Marker marker : markers) {
                    String iconPath = marker.getIconPath();
                    if (iconPath != null && !iconPath.isEmpty() && !uniqueMarkers.containsKey(iconPath)) {
                        uniqueMarkers.put(iconPath, marker);
                    }
                }
                
                // Tampilkan GAMBAR ICON + nama untuk setiap unique icon
                for (Marker marker : uniqueMarkers.values()) {
                    String markerType = marker.getMarkerType();
                        
                        String iconName = marker.getIconName();
                        String iconPath = marker.getIconPath();
                        
                        // Gunakan iconName jika ada (emoji), atau extract dari iconPath
                        String icon = "📍"; // Default
                        if (iconName != null && !iconName.isEmpty()) {
                            icon = iconName;
                        } else {
                            // Fallback ke iconPath
                            if (iconPath != null && !iconPath.isEmpty()) {
                                if (iconPath.contains("stadium")) icon = "⚽";
                                else if (iconPath.contains("fakultas")) icon = "🎓";
                                else if (iconPath.contains("building") || iconPath.contains("gedung")) icon = "🏢";
                                else if (iconPath.contains("apartment")) icon = "🏘️";
                                else if (iconPath.contains("masjid")) icon = "🕌";
                                else if (iconPath.contains("musholla")) icon = "🕌";
                                else if (iconPath.contains("perpustakaan")) icon = "📚";
                                else if (iconPath.contains("landmark")) icon = "🏛️";
                                else if (iconPath.contains("eiffel")) icon = "🗼";
                            }
                        }
                        
                        // Gunakan markerName yang lebih pendek, bukan markerType
                        String displayName = marker.getMarkerName();
                        if (displayName == null || displayName.isEmpty()) {
                            displayName = formatMarkerTypeName(markerType);
                        }
                        
                    // Tampilkan nama LENGKAP dengan GAMBAR ICON (bukan tulisan)
                    // Pass iconPath untuk ditampilkan sebagai gambar
                    addLegendItem(legendPanel, icon, displayName, PRIMARY_GREEN, marker.getIconPath());
                }
                
                // Jika tidak ada marker dari database, gunakan default
                if (uniqueMarkers.isEmpty()) {
                    addLegendItem(legendPanel, "📍", "Lokasi", PRIMARY_GREEN, null);
                }
            } catch (Exception e) {
                logger.warning("Gagal refresh legenda: " + e.getMessage());
                addLegendItem(legendPanel, "📍", "Lokasi", PRIMARY_GREEN, null);
            }
            
            legendPanel.revalidate();
            legendPanel.repaint();
        });
    }
    
    /**
     * Format marker type name untuk tampilan yang rapi
     */
    private String formatMarkerTypeName(String markerType) {
        if (markerType == null || markerType.isEmpty()) {
            return "Lokasi";
        }
        
        // Remove underscore dan ambil kata pertama yang meaningful
        String[] parts = markerType.split("[_-]");
        if (parts.length > 0) {
            String firstPart = parts[0];
            // Capitalize first letter
            if (firstPart.length() > 0) {
                return firstPart.substring(0, 1).toUpperCase() + 
                       firstPart.substring(1).toLowerCase();
            }
        }
        
        return markerType;
    }
    
    /**
     * Calculate route from start to end location and display on map
     * Using A* pathfinding algorithm
     */
    private void calculateAndShowRoute(String startName, String endName) {
        try {
            // Find start and end positions from waypoints or user position
            GeoPosition startPos = null;
            GeoPosition endPos = null;
            
            // Check if start is user position
            if (startName.equals(USER_MARKER_NAME)) {
                startPos = userPosition;
            } else {
                synchronized (waypoints) {
                    for (CustomWaypoint wp : waypoints) {
                        if (wp.getName().equalsIgnoreCase(startName)) {
                            startPos = wp.getPosition();
                            break;
                        }
                    }
                }
            }
            
            // Check if end is user position
            if (endName.equals(USER_MARKER_NAME)) {
                endPos = userPosition;
            } else {
                synchronized (waypoints) {
                    for (CustomWaypoint wp : waypoints) {
                        if (wp.getName().equalsIgnoreCase(endName)) {
                            endPos = wp.getPosition();
                            break;
                        }
                    }
                }
            }
            
            if (startPos == null || endPos == null) {
                JOptionPane.showMessageDialog(this,
                    "Lokasi tidak ditemukan di map!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Set start and end positions
            startPosition = startPos;
            endPosition = endPos;
            
            logger.info("=== STARTING ROUTE CALCULATION ===");
            logger.info("From: " + startName + " (" + startPosition.getLatitude() + "," + startPosition.getLongitude() + ")");
            logger.info("To: " + endName + " (" + endPosition.getLatitude() + "," + endPosition.getLongitude() + ")");
            
            // Use Google Maps untuk rute yang detail dan mengikuti jalan sebenarnya
            com.mycompany.peta_usu.services.DirectionsService directionsService = 
                new com.mycompany.peta_usu.services.DirectionsService();
            
            com.mycompany.peta_usu.services.DirectionsService.DirectionsResult route = 
                directionsService.getWalkingDirections(
                    startPosition.getLatitude(),
                    startPosition.getLongitude(),
                    endPosition.getLatitude(),
                    endPosition.getLongitude()
                );
            
            if (route == null || route.polyline == null || route.polyline.isEmpty()) {
                logger.severe("Google Maps returned no route!");
                JOptionPane.showMessageDialog(this,
                    "Tidak dapat menemukan rute!\nPeriksa koneksi internet.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            logger.info("✅ Route from Google Maps");
            logger.info("Distance: " + route.distanceKm + " km");
            logger.info("Duration: " + route.durationMinutes + " minutes");
            logger.info("Polyline points: " + route.polyline.size());
            
            // Check if route passes through any closed roads
            List<String> closedRoadWarnings = checkRouteForClosedRoads(route.polyline);
            
            // Use Google Maps polyline (detail dan akurat)
            routePath.clear();
            routePath.addAll(route.polyline);
            routeVisible = true;
            
            // Update waypoints to re-render with route
            logger.info("Calling updateWaypoints()...");
            updateWaypoints();
            logger.info("updateWaypoints() completed");
            
            // Force immediate repaint
            logger.info("Calling mapViewer.revalidate() and repaint()...");
            mapViewer.revalidate();
            mapViewer.repaint();
            logger.info("Repaint called");
            
            // Center map on route
            double centerLat = (startPosition.getLatitude() + endPosition.getLatitude()) / 2;
            double centerLon = (startPosition.getLongitude() + endPosition.getLongitude()) / 2;
            mapViewer.setAddressLocation(new GeoPosition(centerLat, centerLon));
            
            // Show route info
            StringBuilder pathInfo = new StringBuilder();
            if (!route.roadNames.isEmpty()) {
                int maxRoads = Math.min(route.roadNames.size(), 5);
                for (int i = 0; i < maxRoads; i++) {
                    if (i > 0) pathInfo.append(" → ");
                    pathInfo.append(route.roadNames.get(i));
                }
                if (route.roadNames.size() > maxRoads) {
                    pathInfo.append(" → ... (").append(route.roadNames.size() - maxRoads).append(" jalan lainnya)");
                }
            } else if (!route.summary.isEmpty()) {
                pathInfo.append(route.summary);
            } else {
                pathInfo.append("Rute langsung");
            }
            
            logger.info("Showing route info dialog...");
            
            // Build warning HTML if route passes closed roads
            String warningHtml = "";
            if (!closedRoadWarnings.isEmpty()) {
                warningHtml = "<div style='background: #fff3e0; padding: 10px; border-left: 4px solid #ff9800; margin-top: 10px;'>";
                warningHtml += "<p style='color: #e65100; font-weight: bold; margin: 0;'>⚠️ PERINGATAN:</p>";
                for (String warning : closedRoadWarnings) {
                    warningHtml += "<p style='color: #d84315; margin: 5px 0;'>- " + warning + "</p>";
                }
                warningHtml += "</div>";
            }
            
            String statusColor = closedRoadWarnings.isEmpty() ? "#2e7d32" : "#f57c00";
            String statusIcon = closedRoadWarnings.isEmpty() ? "✅" : "⚠️";
            String statusText = closedRoadWarnings.isEmpty() ? 
                "Rute aman, tidak melewati jalan tertutup" : 
                "Rute mungkin melewati jalan tertutup - gunakan rute alternatif";
            
            JOptionPane.showMessageDialog(this,
                String.format(
                    "<html><body style='font-family: Times New Roman; padding: 10px; width: 480px;'>" +
                    "<h3 style='color: #388860;'>🗺️ Rute dari Google Maps</h3>" +
                    "<p><b>Dari:</b> %s</p>" +
                    "<p><b>Ke:</b> %s</p>" +
                    "<p><b>Jarak:</b> %.2f km</p>" +
                    "<p><b>Estimasi Waktu:</b> %d menit (jalan kaki)</p>" +
                    "<p><b>Melewati:</b> %s</p>" +
                    "%s" +
                    "<p style='color: %s; margin-top: 10px;'><b>%s %s</b></p>" +
                    "</body></html>",
                    startName, endName, route.distanceKm, route.durationMinutes, 
                    pathInfo.toString(), warningHtml, statusColor, statusIcon, statusText
                ),
                "Info Rute",
                closedRoadWarnings.isEmpty() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
            
            // Force another repaint after dialog closes
            logger.info("Dialog closed, forcing final repaint...");
            mapViewer.repaint();
            logger.info("=== ROUTE CALCULATION COMPLETE ===");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "ERROR in calculateAndShowRoute", e);
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Calculate distance between two GeoPositions using Haversine formula
     * @return distance in kilometers
     */
    /**
     * Check if route passes through any closed roads and return warnings
     */
    private List<String> checkRouteForClosedRoads(List<GeoPosition> polyline) {
        List<String> warnings = new ArrayList<>();
        
        if (activeClosures.isEmpty()) {
            return warnings;
        }
        
        // Get all closed road IDs
        Set<Integer> closedRoadIds = new HashSet<>();
        for (RoadClosure closure : activeClosures) {
            closedRoadIds.add(closure.getRoadId());
        }
        
        // Fetch roads from database
        RoadDAO roadDAO = new RoadDAO();
        List<Road> allRoads = roadDAO.getAllRoads();
        
        // Find closed roads
        Set<String> warnedRoads = new HashSet<>();
        for (Road road : allRoads) {
            if (!closedRoadIds.contains(road.getRoadId()) && 
                road.getRoadType() != Road.RoadType.CLOSED) {
                continue;
            }
            
            // Check if any polyline point is near this closed road
            for (GeoPosition point : polyline) {
                double dist = getDistanceToRoadSegment(
                    point.getLatitude(), point.getLongitude(),
                    road.getStartLat(), road.getStartLng(),
                    road.getEndLat(), road.getEndLng()
                );
                
                if (dist < 0.03 && !warnedRoads.contains(road.getRoadName())) { // 30 meters
                    warnings.add("Rute melewati jalan tertutup: " + road.getRoadName());
                    warnedRoads.add(road.getRoadName());
                    break;
                }
            }
        }
        
        return warnings;
    }
    
    /**
     * Filter Google Maps route polyline untuk skip segments yang melewati jalan tertutup
     * @param polyline Original polyline from Google Maps
     * @param warnings List untuk collect warning messages
     * @return Filtered polyline (bisa sama dengan original jika tidak ada jalan tertutup)
     */
    private List<GeoPosition> filterRouteByClosedRoads(List<GeoPosition> polyline, List<String> warnings) {
        if (activeClosures.isEmpty()) {
            // No closures, return original path
            return new ArrayList<>(polyline);
        }
        
        // Get all closed road IDs
        Set<Integer> closedRoadIds = new HashSet<>();
        for (RoadClosure closure : activeClosures) {
            closedRoadIds.add(closure.getRoadId());
        }
        
        // Fetch all roads from database to check coordinates
        RoadDAO roadDAO = new RoadDAO();
        List<Road> allRoads = roadDAO.getAllRoads();
        
        // Get all roads that are closed
        List<Road> closedRoads = new ArrayList<>();
        for (Road road : allRoads) {
            if (closedRoadIds.contains(road.getRoadId()) || 
                road.getRoadType() == Road.RoadType.CLOSED) {
                closedRoads.add(road);
            }
        }
        
        if (closedRoads.isEmpty()) {
            return new ArrayList<>(polyline);
        }
        
        List<GeoPosition> filteredPath = new ArrayList<>();
        Set<String> warnedRoads = new HashSet<>(); // Track which roads we already warned about
        int skippedSegments = 0;
        
        // Check each segment of polyline
        for (int i = 0; i < polyline.size(); i++) {
            GeoPosition point = polyline.get(i);
            
            // Check if this point is near any closed road
            boolean nearClosedRoad = false;
            String closedRoadName = "";
            
            for (Road road : closedRoads) {
                // Check if point is close to this closed road (within 50 meters)
                double distanceToRoad = getDistanceToRoadSegment(
                    point.getLatitude(), point.getLongitude(),
                    road.getStartLat(), road.getStartLng(),
                    road.getEndLat(), road.getEndLng()
                );
                
                if (distanceToRoad < 0.05) { // 50 meters threshold in km
                    nearClosedRoad = true;
                    closedRoadName = road.getRoadName();
                    break;
                }
            }
            
            if (nearClosedRoad) {
                // Skip this point (don't add to filtered path)
                skippedSegments++;
                
                // Add warning if not already added for this road
                if (!warnedRoads.contains(closedRoadName)) {
                    warnings.add("Rute melewati jalan tertutup: " + closedRoadName);
                    warnedRoads.add(closedRoadName);
                }
            } else {
                // Keep this point
                filteredPath.add(point);
            }
        }
        
        if (skippedSegments > 0) {
            logger.warning("⚠️ Filtered out " + skippedSegments + " points near closed roads");
        }
        
        // If filter removed too many points, return original to avoid broken route
        if (filteredPath.size() < polyline.size() / 2) {
            logger.warning("⚠️ Filter removed too many points (" + filteredPath.size() + "/" + polyline.size() + "), using original route");
            warnings.add("Filter terlalu agresif, menampilkan rute asli dengan warning");
            return new ArrayList<>(polyline);
        }
        
        return filteredPath;
    }
    
    /**
     * Calculate distance from point to line segment (road)
     * Uses perpendicular distance formula
     */
    private double getDistanceToRoadSegment(double pointLat, double pointLng,
                                           double roadStartLat, double roadStartLng,
                                           double roadEndLat, double roadEndLng) {
        // Convert to simple distance for rough check
        // More accurate: use point-to-line-segment distance formula
        
        // Distance from point to start of road
        double distToStart = haversineDistance(pointLat, pointLng, roadStartLat, roadStartLng);
        
        // Distance from point to end of road
        double distToEnd = haversineDistance(pointLat, pointLng, roadEndLat, roadEndLng);
        
        // Length of road segment
        double roadLength = haversineDistance(roadStartLat, roadStartLng, roadEndLat, roadEndLng);
        
        // If road is very short, use minimum of distToStart and distToEnd
        if (roadLength < 0.001) { // Less than 1 meter
            return Math.min(distToStart, distToEnd);
        }
        
        // Check if point projection falls on the road segment
        // Using dot product to find projection point
        double t = ((pointLat - roadStartLat) * (roadEndLat - roadStartLat) + 
                   (pointLng - roadStartLng) * (roadEndLng - roadStartLng)) /
                  (Math.pow(roadEndLat - roadStartLat, 2) + Math.pow(roadEndLng - roadStartLng, 2));
        
        if (t < 0) {
            // Closest to start point
            return distToStart;
        } else if (t > 1) {
            // Closest to end point
            return distToEnd;
        } else {
            // Closest to somewhere on the segment
            double projLat = roadStartLat + t * (roadEndLat - roadStartLat);
            double projLng = roadStartLng + t * (roadEndLng - roadStartLng);
            return haversineDistance(pointLat, pointLng, projLat, projLng);
        }
    }
    
    /**
     * Haversine distance calculation between two lat/lng points
     */
    private double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
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
     * Draw arrow for road direction
     */
    private void drawRoadArrow(Graphics2D g, int x1, int y1, int x2, int y2, Color color) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 12; // Increased from 8 to 12 for better visibility
        
        // Arrow point at 60% of the line (moved closer to middle)
        int midX = (int)(x1 + 0.6 * (x2 - x1));
        int midY = (int)(y1 + 0.6 * (y2 - y1));
        
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
        
        // Draw white outline for better visibility
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(3));
        g.drawPolygon(xPoints, yPoints, 3);
        
        // Fill with road color
        g.setColor(color);
        g.fillPolygon(xPoints, yPoints, 3);
    }
    
    /**
     * Draw small arrow for road direction (less intrusive)
     */
    private void drawSmallRoadArrow(Graphics2D g, int x1, int y1, int x2, int y2, Color color, int midX, int midY) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 6; // Smaller arrow
        
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        
        // Arrow tip at midpoint
        xPoints[0] = midX;
        yPoints[0] = midY;
        
        // Left wing
        xPoints[1] = (int)(midX - arrowSize * Math.cos(angle - Math.PI / 6));
        yPoints[1] = (int)(midY - arrowSize * Math.sin(angle - Math.PI / 6));
        
        // Right wing
        xPoints[2] = (int)(midX - arrowSize * Math.cos(angle + Math.PI / 6));
        yPoints[2] = (int)(midY - arrowSize * Math.sin(angle + Math.PI / 6));
        
        // Fill with road color (no outline for cleaner look)
        g.setColor(color);
        g.fillPolygon(xPoints, yPoints, 3);
    }
    
    /**
     * Update combo boxes with loaded waypoints
     */
    private void updateLocationComboBoxes() {
        SwingUtilities.invokeLater(() -> {
            List<String> lokasiUSU = new ArrayList<>();
            
            // Add user position FIRST
            lokasiUSU.add(USER_MARKER_NAME);
            
            // Collect all location names from waypoints
            synchronized (waypoints) {
                for (CustomWaypoint wp : waypoints) {
                    lokasiUSU.add(wp.getName());
                }
            }
            
            // Don't sort - keep user position at top
            
            logger.info("Updating combo boxes with " + lokasiUSU.size() + " locations");
            
            // Update titik awal combo
            if (titikAwalCombo != null) {
                titikAwalCombo.removeAllItems();
                titikAwalCombo.addItem(""); // Add empty option
                for (String lokasi : lokasiUSU) {
                    titikAwalCombo.addItem(lokasi);
                }
                logger.info("Titik Awal combo updated with " + titikAwalCombo.getItemCount() + " items");
            } else {
                logger.warning("titikAwalCombo is null!");
            }
            
            // Update titik tujuan combo
            if (titikTujuanCombo != null) {
                titikTujuanCombo.removeAllItems();
                titikTujuanCombo.addItem(""); // Add empty option
                for (String lokasi : lokasiUSU) {
                    titikTujuanCombo.addItem(lokasi);
                }
                logger.info("Titik Tujuan combo updated with " + titikTujuanCombo.getItemCount() + " items");
            } else {
                logger.warning("titikTujuanCombo is null!");
            }
        });
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

                // Draw all roads from database (background layer)
                if (roads != null && !roads.isEmpty()) {
                    for (Road road : roads) {
                        // Determine color based on road type and closure status
                        Color roadColor = Color.BLACK; // Default: normal road
                        int strokeWidth = 5; // Increased for better visibility
                        boolean isDashed = false;
                        
                        // PRIORITY 1: Check RoadType.CLOSED first (highest priority)
                        if (road.getRoadType() == Road.RoadType.CLOSED) {
                            roadColor = new Color(220, 20, 60); // Red - jalan tertutup
                            strokeWidth = 6; // Lebih tebal untuk jalan tertutup
                        }
                        // PRIORITY 2: Check closure status from RoadClosure table
                        else if (closureMap.containsKey(road.getRoadId())) {
                            RoadClosure closure = closureMap.get(road.getRoadId());
                            if (closure.getClosureType() == RoadClosure.ClosureType.PERMANENT) {
                                roadColor = new Color(220, 20, 60); // Red - jalan tertutup permanen
                                strokeWidth = 6;
                            } else if (closure.getClosureType() == RoadClosure.ClosureType.TEMPORARY) {
                                roadColor = new Color(255, 140, 0); // Orange - jalan tertutup sementara
                                strokeWidth = 6;
                            }
                        }
                        // PRIORITY 3: Check if ONE_WAY or TWO_WAY
                        else if (road.getRoadType() == Road.RoadType.ONE_WAY) {
                            roadColor = new Color(0, 100, 200); // Blue for one-way
                            isDashed = true;
                        } else if (road.getRoadType() == Road.RoadType.TWO_WAY) {
                            roadColor = new Color(34, 139, 34); // Green for two-way
                            isDashed = true;
                        }
                        
                        // Set stroke style
                        g.setColor(roadColor);
                        if (isDashed) {
                            g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, 
                                BasicStroke.JOIN_ROUND, 0, new float[]{10, 5}, 0));
                        } else {
                            g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        }
                        
                        // Check if road has Google Maps polyline
                        if (road.getPolylinePoints() != null && !road.getPolylinePoints().isEmpty()) {
                            // Use detailed polyline from Google Maps API
                            List<GeoPosition> polyline = decodePolyline(road.getPolylinePoints());
                            
                            if (polyline.size() > 1) {
                                // Draw polyline yang mengikuti jalan sebenarnya dari Google Maps
                                for (int i = 0; i < polyline.size() - 1; i++) {
                                    GeoPosition p1 = polyline.get(i);
                                    GeoPosition p2 = polyline.get(i + 1);
                                    
                                    Point2D pt1 = map.getTileFactory().geoToPixel(p1, map.getZoom());
                                    Point2D pt2 = map.getTileFactory().geoToPixel(p2, map.getZoom());
                                    
                                    Rectangle viewportBounds = map.getViewportBounds();
                                    int px1 = (int)(pt1.getX() - viewportBounds.getX());
                                    int py1 = (int)(pt1.getY() - viewportBounds.getY());
                                    int px2 = (int)(pt2.getX() - viewportBounds.getX());
                                    int py2 = (int)(pt2.getY() - viewportBounds.getY());
                                    
                                    g.drawLine(px1, py1, px2, py2);
                                }
                                
                                // Draw arrow at midpoint untuk direction
                                int midIdx = polyline.size() / 2;
                                if (midIdx > 0 && midIdx < polyline.size()) {
                                    GeoPosition p1 = polyline.get(midIdx - 1);
                                    GeoPosition p2 = polyline.get(midIdx);
                                    
                                    Point2D pt1 = map.getTileFactory().geoToPixel(p1, map.getZoom());
                                    Point2D pt2 = map.getTileFactory().geoToPixel(p2, map.getZoom());
                                    
                                    Rectangle viewportBounds = map.getViewportBounds();
                                    int px1 = (int)(pt1.getX() - viewportBounds.getX());
                                    int py1 = (int)(pt1.getY() - viewportBounds.getY());
                                    int px2 = (int)(pt2.getX() - viewportBounds.getX());
                                    int py2 = (int)(pt2.getY() - viewportBounds.getY());
                                    
                                    drawSmallRoadArrow(g, px1, py1, px2, py2, roadColor, (px1 + px2) / 2, (py1 + py2) / 2);
                                }
                            }
                        } else {
                            // Fallback: Draw straight line jika belum ada polyline dari Google Maps
                            GeoPosition start = new GeoPosition(road.getStartLat(), road.getStartLng());
                            GeoPosition end = new GeoPosition(road.getEndLat(), road.getEndLng());
                            
                            Point2D startPixel = map.getTileFactory().geoToPixel(start, map.getZoom());
                            Point2D endPixel = map.getTileFactory().geoToPixel(end, map.getZoom());
                            
                            Rectangle viewportBounds = map.getViewportBounds();
                            int x1 = (int)(startPixel.getX() - viewportBounds.getX());
                            int y1 = (int)(startPixel.getY() - viewportBounds.getY());
                            int x2 = (int)(endPixel.getX() - viewportBounds.getX());
                            int y2 = (int)(endPixel.getY() - viewportBounds.getY());
                            
                            g.drawLine(x1, y1, x2, y2);
                            
                            // Draw small circle at endpoints
                            g.fillOval(x1 - 2, y1 - 2, 4, 4);
                            g.fillOval(x2 - 2, y2 - 2, 4, 4);
                            
                            // Draw arrow for direction
                            int midX = (x1 + x2) / 2;
                            int midY = (y1 + y2) / 2;
                            drawSmallRoadArrow(g, x1, y1, x2, y2, roadColor, midX, midY);
                        }
                    }
                }

                // Draw route polyline if routing is active
                if (routeVisible && !routePath.isEmpty()) {
                    logger.info("🎨 RENDERING ROUTE: routeVisible=" + routeVisible + ", routePath.size()=" + routePath.size());
                    
                    // Draw YELLOW OUTLINE first for maximum visibility
                    g.setColor(new Color(255, 235, 59)); // Bright Yellow
                    g.setStroke(new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    for (int i = 0; i < routePath.size() - 1; i++) {
                        Point2D p1 = map.getTileFactory().geoToPixel(routePath.get(i), map.getZoom());
                        Point2D p2 = map.getTileFactory().geoToPixel(routePath.get(i + 1), map.getZoom());
                        
                        Rectangle viewportBounds = map.getViewportBounds();
                        int x1 = (int)(p1.getX() - viewportBounds.getX());
                        int y1 = (int)(p1.getY() - viewportBounds.getY());
                        int x2 = (int)(p2.getX() - viewportBounds.getX());
                        int y2 = (int)(p2.getY() - viewportBounds.getY());
                        
                        g.drawLine(x1, y1, x2, y2);
                    }
                    
                    // Draw SKY BLUE route on top - BIRU LANGIT!
                    g.setColor(new Color(135, 206, 235)); // Sky Blue
                    g.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    for (int i = 0; i < routePath.size() - 1; i++) {
                        Point2D p1 = map.getTileFactory().geoToPixel(routePath.get(i), map.getZoom());
                        Point2D p2 = map.getTileFactory().geoToPixel(routePath.get(i + 1), map.getZoom());
                        
                        Rectangle viewportBounds = map.getViewportBounds();
                        int x1 = (int)(p1.getX() - viewportBounds.getX());
                        int y1 = (int)(p1.getY() - viewportBounds.getY());
                        int x2 = (int)(p2.getX() - viewportBounds.getX());
                        int y2 = (int)(p2.getY() - viewportBounds.getY());
                        
                        g.drawLine(x1, y1, x2, y2);
                    }
                    
                    logger.info("✅ Route rendering completed!");
                    
                    // Draw start marker (green circle with white center)
                    if (startPosition != null) {
                        Point2D startPixel = map.getTileFactory().geoToPixel(startPosition, map.getZoom());
                        Rectangle viewportBounds = map.getViewportBounds();
                        int sx = (int)(startPixel.getX() - viewportBounds.getX());
                        int sy = (int)(startPixel.getY() - viewportBounds.getY());
                        
                        g.setColor(new Color(52, 168, 83)); // Green
                        g.fillOval(sx - 10, sy - 10, 20, 20);
                        g.setColor(Color.WHITE);
                        g.fillOval(sx - 5, sy - 5, 10, 10);
                        
                        // Label "A"
                        g.setColor(new Color(52, 168, 83));
                        g.setFont(new Font("Arial", Font.BOLD, 14));
                        g.drawString("A", sx - 4, sy + 5);
                    }
                    
                    // Draw end marker (red circle with white center)
                    if (endPosition != null) {
                        Point2D endPixel = map.getTileFactory().geoToPixel(endPosition, map.getZoom());
                        Rectangle viewportBounds = map.getViewportBounds();
                        int ex = (int)(endPixel.getX() - viewportBounds.getX());
                        int ey = (int)(endPixel.getY() - viewportBounds.getY());
                        
                        g.setColor(new Color(234, 67, 53)); // Red
                        g.fillOval(ex - 10, ey - 10, 20, 20);
                        g.setColor(Color.WHITE);
                        g.fillOval(ex - 5, ey - 5, 10, 10);
                        
                        // Label "B"
                        g.setColor(new Color(234, 67, 53));
                        g.setFont(new Font("Arial", Font.BOLD, 14));
                        g.drawString("B", ex - 4, ey + 5);
                    }
                } else {
                    if (!routeVisible) {
                        logger.warning("⚠️ Route NOT rendered: routeVisible is FALSE");
                    }
                    if (routePath.isEmpty()) {
                        logger.warning("⚠️ Route NOT rendered: routePath is EMPTY");
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
                        // Convert geo position to pixel using tile factory (consistent with Google Maps)
                        Point2D pixel = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());
                        Rectangle viewportBounds = map.getViewportBounds();
                        int x = (int)(pixel.getX() - viewportBounds.getX());
                        int y = (int)(pixel.getY() - viewportBounds.getY());

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
                
                // Draw USER POSITION MARKER (16px, draggable, blue with location pin icon)
                if (userPosition != null) {
                    Point2D userPixel = map.getTileFactory().geoToPixel(userPosition, map.getZoom());
                    Rectangle viewportBounds = map.getViewportBounds();
                    int ux = (int)(userPixel.getX() - viewportBounds.getX());
                    int uy = (int)(userPixel.getY() - viewportBounds.getY());
                    
                    // Draw outer circle (blue)
                    g.setColor(new Color(33, 150, 243)); // Material Blue
                    g.fillOval(ux - 8, uy - 8, 16, 16);
                    
                    // Draw inner circle (white)
                    g.setColor(Color.WHITE);
                    g.fillOval(ux - 5, uy - 5, 10, 10);
                    
                    // Draw center dot (blue)
                    g.setColor(new Color(33, 150, 243));
                    g.fillOval(ux - 3, uy - 3, 6, 6);
                    
                    // Draw label "Posisi Saya"
                    g.setFont(new Font("Times New Roman", Font.BOLD, 11));
                    FontMetrics fm = g.getFontMetrics();
                    String label = "Posisi Saya";
                    int labelWidth = fm.stringWidth(label);
                    
                    g.setColor(new Color(255, 255, 255, 240));
                    g.fillRoundRect(ux - labelWidth/2 - 6, uy + 12, labelWidth + 12, 18, 6, 6);
                    g.setColor(new Color(33, 150, 243));
                    g.drawString(label, ux - labelWidth/2, uy + 25);
                }
            }
        };

        waypointPainter.setWaypoints(waypoints);

        // Set painter tanpa area polygon
        mapViewer.setOverlayPainter(waypointPainter);
        
        // Force repaint to show route immediately
        mapViewer.repaint();

        // Remove ONLY old custom mouse listeners (preserve PanMouseInputListener)
        MouseListener[] listeners = mapViewer.getMouseListeners();
        for (MouseListener ml : listeners) {
            // Keep the pan listener, remove anonymous custom listeners
            if (ml != panListener && ml.getClass().isAnonymousClass()) {
                mapViewer.removeMouseListener(ml);
            }
        }
        
        // Remove old mouse motion listeners (preserve PanMouseInputListener)
        java.awt.event.MouseMotionListener[] motionListeners = mapViewer.getMouseMotionListeners();
        for (java.awt.event.MouseMotionListener mml : motionListeners) {
            // Keep the pan listener, remove anonymous custom listeners
            if (mml != panListener && mml.getClass().isAnonymousClass()) {
                mapViewer.removeMouseMotionListener(mml);
            }
        }

        // Add custom listener for user marker dragging
        // IMPORTANT: Don't use e.consume() - just set flag and let pan listener check it
        MouseAdapter markerMouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Check if clicking on user marker
                if (userPosition != null) {
                    Point2D userPixel = mapViewer.getTileFactory().geoToPixel(userPosition, mapViewer.getZoom());
                    Rectangle viewportBounds = mapViewer.getViewportBounds();
                    int ux = (int)(userPixel.getX() - viewportBounds.getX());
                    int uy = (int)(userPixel.getY() - viewportBounds.getY());
                    
                    if (e.getPoint().distance(new Point(ux, uy)) < 15) {
                        isDraggingUserMarker = true;
                        logger.info("Started dragging user marker");
                    }
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDraggingUserMarker) {
                    isDraggingUserMarker = false;
                    logger.info("Stopped dragging user marker at: " + userPosition);
                    
                    // Update combo boxes after moving user position
                    updateLocationComboBoxes();
                }
            }
        };
        
        java.awt.event.MouseMotionAdapter markerMotionListener = new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDraggingUserMarker) {
                    // Convert screen coordinates to geo position
                    Point screenPoint = e.getPoint();
                    Rectangle viewportBounds = mapViewer.getViewportBounds();
                    
                    // Calculate world coordinates
                    Point2D worldPoint = new Point2D.Double(
                        screenPoint.x + viewportBounds.getX(),
                        screenPoint.y + viewportBounds.getY()
                    );
                    
                    // Convert to geo position
                    GeoPosition newPos = mapViewer.getTileFactory().pixelToGeo(worldPoint, mapViewer.getZoom());
                    userPosition = newPos;
                    
                    // Immediate repaint to show smooth dragging
                    mapViewer.repaint();
                }
            }
        };
        
        mapViewer.addMouseListener(markerMouseListener);
        mapViewer.addMouseMotionListener(markerMotionListener);
        
        // Add click listener for showing waypoint info (separate from drag)
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1 && !isDraggingUserMarker) {
                    Point point = e.getPoint();
                    
                    // Check if clicking on user marker - do nothing (already handled by drag listener)
                    if (userPosition != null) {
                        Point2D userPixel = mapViewer.getTileFactory().geoToPixel(userPosition, mapViewer.getZoom());
                        Rectangle viewportBounds = mapViewer.getViewportBounds();
                        int ux = (int)(userPixel.getX() - viewportBounds.getX());
                        int uy = (int)(userPixel.getY() - viewportBounds.getY());
                        
                        if (point.distance(new Point(ux, uy)) < 15) {
                            return; // Don't show info for user marker
                        }
                    }
                    
                    // Check waypoints
                    synchronized (waypoints) {
                        for (CustomWaypoint wp : waypoints) {
                            Point2D pixel = mapViewer.getTileFactory().geoToPixel(wp.getPosition(), mapViewer.getZoom());
                            Rectangle viewportBounds = mapViewer.getViewportBounds();
                            int wpX = (int)(pixel.getX() - viewportBounds.getX());
                            int wpY = (int)(pixel.getY() - viewportBounds.getY());
                            Point2D waypointPoint = new Point2D.Double(wpX, wpY);
                            
                            if (point.distance(waypointPoint) < 20) {
                                showWaypointInfo(wp);
                                break;
                            }
                        }
                    }
                }
            }
        });
        
        // Add mouse motion listener for dragging
        mapViewer.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDraggingUserMarker) {
                    // Convert screen coordinates to geo position
                    Point screenPoint = e.getPoint();
                    Rectangle viewportBounds = mapViewer.getViewportBounds();
                    
                    // Calculate world coordinates
                    Point2D worldPoint = new Point2D.Double(
                        screenPoint.x + viewportBounds.getX(),
                        screenPoint.y + viewportBounds.getY()
                    );
                    
                    // Convert to geo position
                    GeoPosition newPos = mapViewer.getTileFactory().pixelToGeo(worldPoint, mapViewer.getZoom());
                    userPosition = newPos;
                    
                    // Immediate repaint to show smooth dragging
                    mapViewer.repaint();
                    
                    // Log every few updates to avoid spam
                    if (System.currentTimeMillis() % 100 < 20) {
                        logger.fine("Dragging user marker to: " + userPosition);
                    }
                }
            }
        });

        logger.info("Waypoints updated and painted on map");
    }

    /**
     * Public method to refresh legend - can be called after admin adds new markers
     */
    public void refreshLegendFromDatabase() {
        SwingUtilities.invokeLater(() -> {
            refreshLegend();
            updateWaypoints();
            logger.info("Legend and waypoints refreshed from database");
        });
    }
    
    /**
     * Update layout based on screen width
     * Switch between desktop (sidebar left) and mobile (sidebar bottom) layout
     */
    private void updateLayoutForScreenSize() {
        int width = getWidth();
        boolean shouldBeMobile = width < MOBILE_BREAKPOINT;
        
        // Only update if layout mode changes
        if (shouldBeMobile != isMobileLayout) {
            isMobileLayout = shouldBeMobile;
            
            // Remove existing components
            mainPanel.remove(infoPanel);
            mainPanel.remove(centerContainer);
            
            if (isMobileLayout) {
                // Mobile layout: info panel at bottom
                mainPanel.add(centerContainer, BorderLayout.CENTER);
                
                // Make info panel scrollable for mobile
                JScrollPane infoScrollPane = new JScrollPane(infoPanel);
                infoScrollPane.setPreferredSize(new Dimension(0, 150));
                infoScrollPane.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, PRIMARY_GREEN));
                infoScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                infoScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                
                mainPanel.add(infoScrollPane, BorderLayout.SOUTH);
                
                // Update info panel width for mobile
                infoPanel.setPreferredSize(new Dimension(width - 20, 140));
            } else {
                // Desktop layout: info panel at left
                infoPanel.setPreferredSize(new Dimension(250, 0));
                mainPanel.add(infoPanel, BorderLayout.WEST);
                mainPanel.add(centerContainer, BorderLayout.CENTER);
            }
            
            // Refresh layout
            mainPanel.revalidate();
            mainPanel.repaint();
            
            logger.info("Layout switched to: " + (isMobileLayout ? "MOBILE" : "DESKTOP") + " (width: " + width + "px)");
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin kembali ke halaman utama?",
                "Konfirmasi",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            
            // Return to welcome screen (halaman hijau)
            PETA_USU.main(new String[]{});
        }
    }
    
    /**
     * Decode Google Maps encoded polyline
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
    
    /**
     * Show dialog for reporting road conditions
     */
    private void showReportDialog() {
        JDialog reportDialog = new JDialog(this, "📢 Lapor Kondisi Jalan", true);
        reportDialog.setSize(500, 550);
        reportDialog.setLocationRelativeTo(this);
        reportDialog.setLayout(new BorderLayout(10, 10));
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Laporan Kondisi Jalan");
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_GREEN);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JLabel subtitleLabel = new JLabel("Bantu kami menjaga keamanan dan kenyamanan kampus");
        subtitleLabel.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Location field
        JLabel lblLocation = new JLabel("Lokasi:");
        lblLocation.setFont(MAIN_FONT);
        lblLocation.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(lblLocation);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JTextField txtLocation = new JTextField();
        txtLocation.setFont(MAIN_FONT);
        txtLocation.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        txtLocation.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(txtLocation);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Coordinates (auto-filled from current map center)
        GeoPosition center = (mapViewer != null) ? mapViewer.getCenterPosition() 
            : new GeoPosition(3.5688, 98.6566); // Default USU coordinates
        JLabel lblCoords = new JLabel(String.format("Koordinat: %.6f, %.6f", 
            center.getLatitude(), center.getLongitude()));
        lblCoords.setFont(new Font("Times New Roman", Font.ITALIC, 11));
        lblCoords.setForeground(Color.GRAY);
        lblCoords.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(lblCoords);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Report type
        JLabel lblType = new JLabel("Tipe Laporan:");
        lblType.setFont(MAIN_FONT);
        lblType.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(lblType);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JComboBox<Report.ReportType> cboType = new JComboBox<>(Report.ReportType.values());
        cboType.setFont(MAIN_FONT);
        cboType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cboType.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(cboType);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Description
        JLabel lblDesc = new JLabel("Deskripsi:");
        lblDesc.setFont(MAIN_FONT);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(lblDesc);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JTextArea txtDescription = new JTextArea(5, 20);
        txtDescription.setFont(MAIN_FONT);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        scrollDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollDesc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        mainPanel.add(scrollDesc);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Buttons panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton btnCancel = new JButton("Batal");
        btnCancel.setFont(MAIN_FONT);
        btnCancel.addActionListener(e -> reportDialog.dispose());
        
        JButton btnSubmit = new JButton("Kirim Laporan");
        btnSubmit.setFont(new Font("Times New Roman", Font.BOLD, 12));
        btnSubmit.setBackground(PRIMARY_GREEN);
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFocusPainted(false);
        btnSubmit.addActionListener(e -> {
            String location = txtLocation.getText().trim();
            String description = txtDescription.getText().trim();
            
            if (location.isEmpty()) {
                JOptionPane.showMessageDialog(reportDialog, 
                    "Lokasi tidak boleh kosong!", 
                    "Validasi", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (description.isEmpty()) {
                JOptionPane.showMessageDialog(reportDialog, 
                    "Deskripsi tidak boleh kosong!", 
                    "Validasi", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Create report object
            Report report = new Report();
            report.setUserNim("GUEST");
            report.setUserName("Guest User");
            report.setLocation(location);
            report.setLatitude(center.getLatitude());
            report.setLongitude(center.getLongitude());
            report.setDescription(description);
            report.setReportType((Report.ReportType) cboType.getSelectedItem());
            
            // Save to database
            ReportDAO reportDAO = new ReportDAO();
            boolean success = reportDAO.createReport(report);
            
            if (success) {
                JOptionPane.showMessageDialog(reportDialog,
                    "Terima kasih! Laporan Anda telah diterima dan akan segera ditindaklanjuti.",
                    "Laporan Terkirim",
                    JOptionPane.INFORMATION_MESSAGE);
                reportDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(reportDialog,
                    "Gagal mengirim laporan. Silakan coba lagi.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);
        mainPanel.add(btnPanel);
        
        reportDialog.add(mainPanel, BorderLayout.CENTER);
        reportDialog.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
