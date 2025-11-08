/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.peta_usu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import org.json.*;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import java.awt.event.MouseListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 *
 * @author ASUS
 */
public class MapFrame extends javax.swing.JFrame {
     private JXMapViewer mapViewer;
    private String studentNim;
    private Set<CustomWaypoint> waypoints;
    private JDialog loadingDialog;
    private JComboBox<String> titikAwalCombo;
    private JComboBox<String> titikTujuanCombo;
    private org.jxmapviewer.painter.Painter<JXMapViewer> areaPolygonPainter = null;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MapFrame.class.getName());
    
    private static final Color PRIMARY_GREEN = new Color(0x388860);
    private static final Color LIGHT_GREEN = new Color(0x4CAF6E);
    private static final Font MAIN_FONT = new Font("Times New Roman", Font.PLAIN, 14);
    private static final Font TITLE_FONT = new Font("Times New Roman", Font.BOLD, 28);
    private static final Font SUBTITLE_FONT = new Font("Times New Roman", Font.PLAIN, 14);
    private static final Font LEGEND_FONT = new Font("Times New Roman", Font.BOLD, 16);
    
    /**
     * Creates new form MapFrame
     */
    public MapFrame(String nim) {
        this.studentNim = nim;
        this.waypoints = new HashSet<>();
        showLoadingDialog();
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                initComponents();
                setupMapUI();
                loadGeoJSONData();
               
                Thread.sleep(2000);
                return null;
            }
            
            @Override
            protected void done() {
                hideLoadingDialog();
            }
        };
        worker.execute();
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
        setTitle("Peta Kampus USU - " + studentNim);
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(Color.WHITE);
        
        JPanel routePanel = createRouteSelectionPanel();
        centerContainer.add(routePanel, BorderLayout.NORTH);
        
        JPanel mapPanel = createMapPanel();
        centerContainer.add(mapPanel, BorderLayout.CENTER);
        
        mainPanel.add(centerContainer, BorderLayout.CENTER);

        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.EAST);

        setContentPane(mainPanel);
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

        List<String> lokasiUSU = new ArrayList<>();
        try {
            File file = new File("resources/kampus_usu.geojson");
            if (file.exists()) {
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONObject geoJson = new JSONObject(content);
                JSONArray features = geoJson.getJSONArray("features");
                for (int i = 0; i < features.length(); i++) {
                    JSONObject properties = features.getJSONObject(i).getJSONObject("properties");
                    lokasiUSU.add(properties.getString("name"));
                }
            }
        } catch (Exception e) {
            logger.warning("Gagal memuat daftar lokasi untuk combobox: " + e.getMessage());
        }

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
                JOptionPane.showMessageDialog(this,
                    "Rute dari \"" + awal + "\" ke \"" + tujuan + "\" akan ditampilkan (fitur segera hadir).",
                    "Info Rute",
                    JOptionPane.INFORMATION_MESSAGE);
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
        loadUSUAreaPolygon();
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

     private void loadGeoJSONData() {
        try {
            File file = new File("resources/kampus_usu.geojson");
            if (!file.exists()) {
                logger.warning("GeoJSON file not found at: " + file.getAbsolutePath());
                JOptionPane.showMessageDialog(this,
                    "File GeoJSON tidak ditemukan!\nPastikan file ada di: resources/kampus_usu.geojson",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
                updateWaypoints();
                return;
            }

            String content = new String(Files.readAllBytes(file.toPath()));
            JSONObject geoJson = new JSONObject(content);
            JSONArray features = geoJson.getJSONArray("features");

            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                JSONObject geometry = feature.getJSONObject("geometry");
                JSONObject properties = feature.getJSONObject("properties");

                if (geometry.getString("type").equals("Point")) {
                JSONArray coordinates = geometry.getJSONArray("coordinates");
                double lon = coordinates.getDouble(0);
                double lat = coordinates.getDouble(1);

                String name = properties.optString("name", "Unknown");
                String type = properties.optString("type", "Other");
                String description = properties.optString("description", "");
                String icon = properties.optString("icon", "📍"); 

                addMarker(lat, lon, name, type, description, icon);
            }

            }
            
            logger.info("Loaded " + waypoints.size() + " markers from GeoJSON");

        } catch (Exception e) {
            logger.severe("Error loading GeoJSON: " + e.getMessage());
            e.printStackTrace();
        }
        
        updateWaypoints();
    }
     
    private void loadUSUAreaPolygon() {
        try {
            File file = new File("resources/area_usu.geojson");
            if (!file.exists()) {
                logger.warning("File area_usu.geojson tidak ditemukan di: " + file.getAbsolutePath());
                return;
            }

            String content = new String(Files.readAllBytes(file.toPath()));
            JSONObject geoJson = new JSONObject(content);
            JSONArray features = geoJson.getJSONArray("features");

            java.util.List<GeoPosition> polygonPoints = new java.util.ArrayList<>();

            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                JSONObject geometry = feature.getJSONObject("geometry");
                if (!geometry.getString("type").equalsIgnoreCase("MultiPolygon")) continue;

                JSONArray multiPoly = geometry.getJSONArray("coordinates");
                for (int j = 0; j < multiPoly.length(); j++) {
                    JSONArray polygon = multiPoly.getJSONArray(j).getJSONArray(0);
                    for (int k = 0; k < polygon.length(); k++) {
                        JSONArray coord = polygon.getJSONArray(k);
                        double lon = coord.getDouble(0);
                        double lat = coord.getDouble(1);
                        polygonPoints.add(new GeoPosition(lat, lon));
                    }
                }
            }

            double minLat = polygonPoints.stream().mapToDouble(GeoPosition::getLatitude).min().orElse(3.56);
            double maxLat = polygonPoints.stream().mapToDouble(GeoPosition::getLatitude).max().orElse(3.57);
            double minLon = polygonPoints.stream().mapToDouble(GeoPosition::getLongitude).min().orElse(98.65);
            double maxLon = polygonPoints.stream().mapToDouble(GeoPosition::getLongitude).max().orElse(98.66);

            GeoPosition center = new GeoPosition((minLat + maxLat) / 2, (minLon + maxLon) / 2);
            mapViewer.setAddressLocation(center);

            mapViewer.setZoom(1); 

            org.jxmapviewer.painter.Painter<JXMapViewer> polygonPainter = (g, map, w, h) -> {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                java.awt.Polygon poly = new java.awt.Polygon();
                for (GeoPosition pos : polygonPoints) {
                    Point2D pt = map.convertGeoPositionToPoint(pos);
                    poly.addPoint((int) pt.getX(), (int) pt.getY());
                }

                g2d.setColor(PRIMARY_GREEN);
                g2d.setStroke(new BasicStroke(4));
                g2d.drawPolygon(poly);

                g2d.setColor(new Color(56, 136, 96, 30)); 
                g2d.fillPolygon(poly);
            };

            org.jxmapviewer.painter.Painter<JXMapViewer> maskPainter = (g, map, w, h) -> {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                java.awt.Polygon poly = new java.awt.Polygon();
                for (GeoPosition pos : polygonPoints) {
                    Point2D pt = map.convertGeoPositionToPoint(pos);
                    poly.addPoint((int) pt.getX(), (int) pt.getY());
                }

                java.awt.Shape outer = new java.awt.Rectangle(0, 0, map.getWidth(), map.getHeight());

                java.awt.geom.Area mask = new java.awt.geom.Area(outer);
                mask.subtract(new java.awt.geom.Area(poly));

                g2d.setColor(new Color(240, 240, 240, 230)); 
                g2d.fill(mask);
            };

            applyCombinedPainters(
                    new org.jxmapviewer.painter.CompoundPainter<>(polygonPainter, maskPainter)
                );

            logger.info("Area USU berhasil dimuat dan difokuskan dengan " + polygonPoints.size() + " titik.");

        } catch (Exception e) {
            logger.severe("Gagal memuat area USU: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applyCombinedPainters(org.jxmapviewer.painter.Painter<JXMapViewer> polygonPainter) {
        this.areaPolygonPainter = polygonPainter;
        updateWaypoints(); 
    }

    private void addMarker(double lat, double lon, String name, String type, String description, String icon) {
        CustomWaypoint waypoint = new CustomWaypoint(name, type, description, icon, new GeoPosition(lat, lon));
        waypoints.add(waypoint);
        logger.info(String.format("Added marker: %s (%s) at (%.6f, %.6f)", name, type, lat, lon));
    }
    
    private void updateWaypoints() {
        WaypointPainter<CustomWaypoint> waypointPainter = new WaypointPainter<CustomWaypoint>() {
            @Override
            protected void doPaint(Graphics2D g, JXMapViewer map, int width, int height) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                for (CustomWaypoint wp : waypoints) {
        Point2D point = mapViewer.convertGeoPositionToPoint(wp.getPosition());
        int x = (int) point.getX();
        int y = (int) point.getY();

        g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        g.drawString(wp.getIcon(), x - 8, y + 6);

        g.setFont(new Font("Times New Roman", Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        int labelWidth = fm.stringWidth(wp.getName());

        g.setColor(new Color(255, 255, 255, 230));
        g.fillRoundRect(x - labelWidth/2 - 4, y + 8, labelWidth + 8, 14, 5, 5);
        g.setColor(Color.BLACK);
        g.drawString(wp.getName(), x - labelWidth/2, y + 19);
         }

        }
    };

        waypointPainter.setWaypoints(waypoints);

        if (areaPolygonPainter != null) {
            org.jxmapviewer.painter.CompoundPainter<JXMapViewer> compoundPainter =
                new org.jxmapviewer.painter.CompoundPainter<>(areaPolygonPainter, waypointPainter);
            mapViewer.setOverlayPainter(compoundPainter);
        } else {
            mapViewer.setOverlayPainter(waypointPainter);
        }

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
                    for (CustomWaypoint wp : waypoints) {
                        Point2D waypointPoint = mapViewer.convertGeoPositionToPoint(wp.getPosition());
                        if (point.distance(waypointPoint) < 15) {
                            showWaypointInfo(wp);
                            break;
                        }
                    }
                }
            }
        });

        logger.info("Waypoints updated and painted on map");
    }

    private static final java.util.Map<String, java.util.Map<String, java.util.List<String>>> fakultasData = new java.util.HashMap<>();
    static {
        fakultasData.put("Fakultas Kedokteran", java.util.Map.of(
            "Gedung A", java.util.List.of("Ruang Anatomi", "Ruang Fisiologi", "Ruang Tutorial 1"),
            "Gedung B", java.util.List.of("Laboratorium Mikrobiologi", "Ruang Patologi", "Ruang Dosen")
        ));

        fakultasData.put("Fakultas Psikologi", java.util.Map.of(
            "Gedung Utama", java.util.List.of("Ruang Kuliah 1", "Ruang Kuliah 2", "Laboratorium Psikologi Eksperimen"),
            "Gedung Konseling", java.util.List.of("Ruang Konseling A", "Ruang Konseling B")
        ));

        fakultasData.put("Fakultas Kesehatan Masyarakat", java.util.Map.of(
            "Gedung A", java.util.List.of("Ruang Epidemiologi", "Ruang Promosi Kesehatan"),
            "Gedung B", java.util.List.of("Laboratorium Kesehatan Lingkungan", "Ruang Seminar")
        ));

        fakultasData.put("Fakultas Keperawatan", java.util.Map.of(
            "Gedung 1", java.util.List.of("Ruang Kelas 1", "Ruang Kelas 2", "Laboratorium Keterampilan Klinik"),
            "Gedung 2", java.util.List.of("Ruang Dosen", "Ruang Diskusi")
        ));

        fakultasData.put("Fakultas Kedokteran Gigi", java.util.Map.of(
            "Gedung Klinik", java.util.List.of("Klinik Gigi 1", "Klinik Gigi 2", "Ruang Sterilisasi"),
            "Gedung Teori", java.util.List.of("Ruang Kuliah 1", "Ruang Kuliah 2")
        ));

        fakultasData.put("Fakultas Ilmu Komputer dan IT", java.util.Map.of(
            "Gedung A", java.util.List.of("Lab Pemrograman", "Lab AI dan Data Science", "Ruang Kuliah A1"),
            "Gedung B", java.util.List.of("Lab Jaringan", "Lab Multimedia", "Ruang Kuliah B2")
        ));

        fakultasData.put("Fakultas Teknik", java.util.Map.of(
            "Gedung Teknik Sipil", java.util.List.of("Ruang Struktur", "Ruang Transportasi", "Lab Mekanika Tanah"),
            "Gedung Teknik Mesin", java.util.List.of("Lab CNC", "Ruang Thermodinamika", "Ruang Produksi"),
            "Gedung Teknik Elektro", java.util.List.of("Lab Listrik", "Ruang Instrumentasi", "Lab Robotika"),
            "Gedung Teknik Industri", java.util.List.of("Ruang Ergonomi", "Lab Simulasi", "Ruang Produksi"),
            "Gedung Teknik Lingkungan", java.util.List.of("Ruang Analisis Air", "Ruang Pengolahan Limbah")
        ));

        fakultasData.put("Fakultas Hukum", java.util.Map.of(
            "Gedung Utama", java.util.List.of("Ruang Kuliah 1", "Ruang Sidang Tiruan", "Ruang Dosen"),
            "Gedung Perpustakaan", java.util.List.of("Ruang Baca", "Ruang Arsip Hukum")
        ));

        fakultasData.put("Fakultas Ekonomi dan Bisnis", java.util.Map.of(
            "Gedung A", java.util.List.of("Ruang Akuntansi", "Ruang Manajemen", "Lab Komputer Ekonomi"),
            "Gedung B", java.util.List.of("Ruang Kuliah 1", "Ruang Kuliah 2")
        ));

        fakultasData.put("Fakultas Ilmu Budaya", java.util.Map.of(
            "Gedung A", java.util.List.of("Ruang Sastra Indonesia", "Ruang Linguistik", "Lab Bahasa"),
            "Gedung B", java.util.List.of("Ruang Dosen", "Ruang Kuliah B1")
        ));

        fakultasData.put("Fakultas Ilmu Sosial dan Politik", java.util.Map.of(
            "Gedung A", java.util.List.of("Ruang Ilmu Pemerintahan", "Ruang Komunikasi", "Ruang Sosiologi"),
            "Gedung B", java.util.List.of("Ruang Kuliah 1", "Ruang Kuliah 2")
        ));

        fakultasData.put("Fakultas Pertanian", java.util.Map.of(
            "Gedung A", java.util.List.of("Ruang Agronomi", "Ruang Tanaman Pangan", "Lab Pertanian"),
            "Gedung B", java.util.List.of("Ruang Hama & Penyakit Tanaman", "Ruang Dosen")
        ));

        fakultasData.put("Fakultas Vokasi", java.util.Map.of(
            "Gedung Utama", java.util.List.of("Ruang Komputer", "Ruang Praktikum Akuntansi", "Ruang Multimedia"),
            "Gedung Workshop", java.util.List.of("Ruang Mesin", "Ruang Teknik Elektro Dasar")
        ));

        fakultasData.put("Fakultas MIPA", java.util.Map.of(
            "Gedung Fisika", java.util.List.of("Lab Fisika Dasar", "Lab Fisika Modern"),
            "Gedung Kimia", java.util.List.of("Lab Kimia Dasar", "Lab Kimia Organik"),
            "Gedung Matematika", java.util.List.of("Ruang Statistika", "Ruang Analisis Numerik"),
            "Gedung Biologi", java.util.List.of("Lab Bioteknologi", "Lab Mikrobiologi")
        ));

        fakultasData.put("Fakultas Farmasi", java.util.Map.of(
            "Gedung A", java.util.List.of("Lab Farmasetika", "Ruang Kimia Farmasi"),
            "Gedung B", java.util.List.of("Lab Mikrobiologi", "Ruang Analisis Obat")
        ));

    }
    private void showWaypointInfo(CustomWaypoint waypoint) {
        if (waypoint.getType().equalsIgnoreCase("Fakultas")) {
            showFakultasDialog(waypoint);
            return;
        }

        String message = String.format(
            "<html><body style='width: 250px; padding: 10px; font-family: Times New Roman;'>" +
            "<h3 style='color: #388860; margin: 0;'>%s</h3>" +
            "<p style='margin: 8px 0;'><b>Tipe:</b> %s</p>" +
            "<p style='margin: 8px 0;'><b>Deskripsi:</b><br>%s</p>" +
            "<p style='margin: 8px 0; color: #666;'><b>Koordinat:</b><br>%.6f, %.6f</p>" +
            "</body></html>",
            waypoint.getName(),
            waypoint.getType(),
            waypoint.getDescription(),
            waypoint.getPosition().getLatitude(),
            waypoint.getPosition().getLongitude()
        );

        JOptionPane.showMessageDialog(
            this,
            message,
            "📍 Informasi Lokasi",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private void showFakultasDialog(CustomWaypoint waypoint) {
        JDialog dialog = new JDialog(this, "📘 " + waypoint.getName(), true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        content.setBackground(Color.WHITE);

        JLabel fakultasLabel = new JLabel(waypoint.getName());
        fakultasLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        fakultasLabel.setForeground(PRIMARY_GREEN);
        fakultasLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel("<html><p style='width:330px;'>" + waypoint.getDescription() + "</p></html>");
        descLabel.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(fakultasLabel);
        content.add(Box.createRigidArea(new Dimension(0, 5)));
        content.add(descLabel);
        content.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel gedungLabel = new JLabel("Pilih Gedung:");
        gedungLabel.setFont(MAIN_FONT);
        gedungLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> gedungCombo = new JComboBox<>();
        gedungCombo.setFont(MAIN_FONT);
        gedungCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        gedungCombo.setMaximumSize(new Dimension(300, 30));

        JLabel ruangLabel = new JLabel("Pilih Ruangan:");
        ruangLabel.setFont(MAIN_FONT);
        ruangLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> ruangCombo = new JComboBox<>();
        ruangCombo.setFont(MAIN_FONT);
        ruangCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        ruangCombo.setMaximumSize(new Dimension(300, 30));

        java.util.Map<String, java.util.List<String>> gedungMap = fakultasData.get(waypoint.getName());
        if (gedungMap != null) {
            for (String gedung : gedungMap.keySet()) {
                gedungCombo.addItem(gedung);
            }

            gedungCombo.addActionListener(e -> {
                ruangCombo.removeAllItems();
                String selectedGedung = (String) gedungCombo.getSelectedItem();
                if (selectedGedung != null) {
                    java.util.List<String> ruangan = gedungMap.get(selectedGedung);
                    if (ruangan != null) {
                        for (String r : ruangan) ruangCombo.addItem(r);
                    }
                }
            });

            gedungCombo.setSelectedIndex(0);
        } else {
            gedungCombo.addItem("Data belum tersedia");
        }

        content.add(gedungLabel);
        content.add(gedungCombo);
        content.add(Box.createRigidArea(new Dimension(0, 10)));
        content.add(ruangLabel);
        content.add(ruangCombo);

        JButton closeButton = new JButton("Tutup");
        closeButton.setBackground(PRIMARY_GREEN);
        closeButton.setForeground(Color.WHITE);
        closeButton.addActionListener(e -> dialog.dispose());
        closeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        closeButton.setMaximumSize(new Dimension(100, 30));

        content.add(Box.createRigidArea(new Dimension(0, 15)));
        content.add(closeButton);

        dialog.add(content, BorderLayout.CENTER);
        dialog.setVisible(true);
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
                "Apakah Anda yakin ingin logout?",
                "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
    
    private static class CustomWaypoint extends DefaultWaypoint {
    private final String name;
    private final String type;
    private final String description;
    private final String icon;

    public CustomWaypoint(String name, String type, String description, String icon, GeoPosition coord) {
        super(coord);
        this.name = name;
        this.type = type;
        this.description = description;
        this.icon = icon;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
}


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
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
