package com.mycompany.peta_usu.ui;

import com.mycompany.peta_usu.dao.*;
import com.mycompany.peta_usu.models.*;
import com.mycompany.peta_usu.utils.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.viewer.*;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.imageio.ImageIO;

/**
 * AdminMapPanel - Panel untuk admin manage markers
 * Fitur: upload icon, drag-drop, save ke database
 * 
 * @author PETA_USU Team
 */
public class AdminMapPanel extends JPanel {
    
    private MarkerDAO markerDAO;
    private BuildingDAO buildingDAO;
    private int currentUserId;
    
    // UI Components
    private JTable markersTable;
    private DefaultTableModel tableModel;
    private JButton btnEditMarker;
    private JButton btnDeleteMarker;
    private JButton btnRefresh;
    private JTextField txtSearch;
    private JComboBox<String> cboMarkerType;
    
    // Map panel dengan Google Maps API
    private JPanel mapPanel;
    private JXMapViewer mapViewer;
    private Set<DraggableWaypoint> waypoints;
    private DraggableWaypoint selectedMarker = null;
    private Point dragStart = null;
    
    public AdminMapPanel(int userId) {
        this.currentUserId = userId;
        this.markerDAO = new MarkerDAO();
        this.buildingDAO = new BuildingDAO();
        this.waypoints = new HashSet<>();
        
        initComponents();
        loadMarkers();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel - Controls
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center - Split pane (Map | Table)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createMapPanel());
        splitPane.setRightComponent(createTablePanel());
        splitPane.setDividerLocation(500);
        add(splitPane, BorderLayout.CENTER);
        
        // Bottom panel - Status
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(new Color(56, 136, 96)); // USU Green
        
        JLabel lblTitle = new JLabel("Marker Management");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        panel.add(lblTitle);
        
        return panel;
    }
    
    private JPanel createMapPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Map View"));
        
        // Map toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnZoomIn = new JButton("+");
        JButton btnZoomOut = new JButton("-");
        JButton btnCenter = new JButton("Center USU");
        JButton btnAddToMap = new JButton("📁 Add Marker to Map");
        
        btnAddToMap.addActionListener(e -> uploadAndAddIconMarker());
        btnCenter.addActionListener(e -> centerMapToUSU());
        
        btnAddToMap.setBackground(new Color(33, 150, 243));
        btnAddToMap.setForeground(Color.WHITE);
        btnAddToMap.setToolTipText("Upload icon dan drag ke posisi di map");
        
        toolbar.add(btnZoomIn);
        toolbar.add(btnZoomOut);
        toolbar.add(btnCenter);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(btnAddToMap);
        
        panel.add(toolbar, BorderLayout.NORTH);
        
        // Map display dengan JXMapViewer
        mapPanel = new JPanel(new BorderLayout());
        mapPanel.setBackground(Color.WHITE);
        
        // Initialize JXMapViewer
        mapViewer = new JXMapViewer();
        
        // Setup Google Maps tiles
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
        
        // Center to USU
        GeoPosition usu = new GeoPosition(GoogleMapsHelper.USU_CENTER_LAT, GoogleMapsHelper.USU_CENTER_LNG);
        mapViewer.setZoom(4);
        mapViewer.setAddressLocation(usu);
        
        // Enable pan but disable during drag
        MouseAdapter panAdapter = new PanMouseInputListener(mapViewer);
        
        // Add drag-drop mouse listeners
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Check if clicking on a marker
                Point clickPoint = e.getPoint();
                selectedMarker = null; // Reset selection
                
                // Calculate current icon size based on zoom
                int zoom = mapViewer.getZoom();
                double zoomFactor = Math.max(0.2, Math.min(1.0, (16 - zoom) / 10.0));
                int iconSize = (int)(28 * zoomFactor);
                
                for (DraggableWaypoint wp : waypoints) {
                    Point2D markerPoint = mapViewer.getTileFactory().geoToPixel(
                        wp.getPosition(), 
                        mapViewer.getZoom()
                    );
                    
                    // Convert to viewport coordinates
                    Rectangle viewportBounds = mapViewer.getViewportBounds();
                    int screenX = (int)(markerPoint.getX() - viewportBounds.x);
                    int screenY = (int)(markerPoint.getY() - viewportBounds.y);
                    
                    // Check if click is within marker bounds (scaled icon size)
                    Rectangle2D bounds = new Rectangle2D.Double(
                        screenX - iconSize/2, 
                        screenY - iconSize, 
                        iconSize, 
                        iconSize
                    );
                    
                    if (bounds.contains(clickPoint)) {
                        selectedMarker = wp;
                        dragStart = clickPoint;
                        mapViewer.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                        System.out.println("Selected marker: " + wp.getName());
                        return;
                    }
                }
                
                // Right click to add marker
                if (e.getButton() == MouseEvent.BUTTON3) {
                    GeoPosition mapPosition = mapViewer.convertPointToGeoPosition(e.getPoint());
                    showAddMarkerDialogAt(mapPosition.getLatitude(), mapPosition.getLongitude());
                } else {
                    // Pan mode
                    panAdapter.mousePressed(e);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (selectedMarker != null) {
                    // Save new position to database
                    saveMarkerPosition(selectedMarker);
                    selectedMarker = null;
                    dragStart = null;
                    mapViewer.setCursor(Cursor.getDefaultCursor());
                } else {
                    panAdapter.mouseReleased(e);
                }
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                panAdapter.mouseClicked(e);
            }
        });
        
        mapViewer.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectedMarker != null && dragStart != null) {
                    // Update marker position
                    GeoPosition newPos = mapViewer.convertPointToGeoPosition(e.getPoint());
                    selectedMarker.setPosition(newPos);
                    mapViewer.repaint();
                } else {
                    panAdapter.mouseDragged(e);
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                panAdapter.mouseMoved(e);
            }
        });
        
        // Mouse wheel zoom
        mapViewer.addMouseWheelListener(e -> {
            int z = mapViewer.getZoom();
            if (e.getWheelRotation() > 0) {
                if (z < 15) mapViewer.setZoom(z + 1);
            } else {
                if (z > 1) mapViewer.setZoom(z - 1);
            }
        });
        
        mapPanel.add(mapViewer, BorderLayout.CENTER);
        panel.add(mapPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void refreshMap() {
        // Load waypoints from database
        waypoints.clear();
        List<Marker> markers = markerDAO.getAllMarkers();
        
        for (Marker marker : markers) {
            if (marker.isActive()) {
                GeoPosition pos = new GeoPosition(marker.getLatitude(), marker.getLongitude());
                DraggableWaypoint wp = new DraggableWaypoint(
                    marker.getMarkerId(),
                    marker.getMarkerName(), 
                    pos,
                    marker.getIconPath()
                );
                waypoints.add(wp);
            }
        }
        
        // Create custom painter untuk drag-drop markers
        DraggableWaypointPainter painter = new DraggableWaypointPainter();
        painter.setWaypoints(waypoints);
        
        // Set painter to map
        mapViewer.setOverlayPainter(painter);
        mapViewer.repaint();
    }
    
    private void centerMapToUSU() {
        GeoPosition usu = new GeoPosition(GoogleMapsHelper.USU_CENTER_LAT, GoogleMapsHelper.USU_CENTER_LNG);
        mapViewer.setAddressLocation(usu);
        mapViewer.setZoom(4);
        JOptionPane.showMessageDialog(this, 
            "Map centered to USU Campus", 
            "Info", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Markers List"));
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        txtSearch = new JTextField(15);
        searchPanel.add(txtSearch);
        
        searchPanel.add(new JLabel("Type:"));
        cboMarkerType = new JComboBox<>(new String[]{
            "All", "Building", "Fakultas", "Gedung", "Fasilitas"
        });
        searchPanel.add(cboMarkerType);
        
        btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadMarkers());
        searchPanel.add(btnRefresh);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "Name", "Type", "Latitude", "Longitude", "Active"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        markersTable = new JTable(tableModel);
        markersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(markersTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        btnEditMarker = new JButton("Edit");
        btnEditMarker.addActionListener(e -> editSelectedMarker());
        
        btnDeleteMarker = new JButton("Delete");
        btnDeleteMarker.setBackground(new Color(244, 67, 54));
        btnDeleteMarker.setForeground(Color.WHITE);
        btnDeleteMarker.addActionListener(e -> deleteSelectedMarker());
        
        buttonsPanel.add(btnEditMarker);
        buttonsPanel.add(btnDeleteMarker);
        
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel statusLabel = new JLabel("Ready");
        panel.add(statusLabel);
        return panel;
    }
    
    private void loadMarkers() {
        tableModel.setRowCount(0);
        
        List<Marker> markers = markerDAO.getAllMarkers();
        
        for (Marker marker : markers) {
            tableModel.addRow(new Object[]{
                marker.getMarkerId(),
                marker.getMarkerName(),
                marker.getMarkerType(),
                marker.getLatitude(),
                marker.getLongitude(),
                marker.isActive() ? "Yes" : "No"
            });
        }
        
        refreshMap();
    }
    
    private void showAddMarkerDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Add New Marker", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Marker Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Marker Name:"), gbc);
        
        gbc.gridx = 1;
        JTextField txtName = new JTextField(20);
        panel.add(txtName, gbc);
        
        // Marker Type
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Type:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<String> cboType = new JComboBox<>(new String[]{
            "Building", "Fakultas", "Gedung", "Fasilitas"
        });
        panel.add(cboType, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        JTextArea txtDescription = new JTextArea(3, 20);
        txtDescription.setLineWrap(true);
        panel.add(new JScrollPane(txtDescription), gbc);
        
        // Latitude
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Latitude:"), gbc);
        
        gbc.gridx = 1;
        JTextField txtLat = new JTextField(String.valueOf(GoogleMapsHelper.USU_CENTER_LAT));
        panel.add(txtLat, gbc);
        
        // Longitude
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Longitude:"), gbc);
        
        gbc.gridx = 1;
        JTextField txtLng = new JTextField(String.valueOf(GoogleMapsHelper.USU_CENTER_LNG));
        panel.add(txtLng, gbc);
        
        // Icon Upload
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Icon:"), gbc);
        
        gbc.gridx = 1;
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblIconPreview = new JLabel("No icon selected");
        lblIconPreview.setPreferredSize(new Dimension(50, 50));
        lblIconPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JButton btnSelectIcon = new JButton("Select Icon");
        final String[] selectedIconPath = {null};
        
        btnSelectIcon.addActionListener(e -> {
            File iconFile = IconUploadManager.selectIconFile((JFrame) SwingUtilities.getWindowAncestor(dialog));
            if (iconFile != null) {
                // Upload with resize to 32x32
                String uploadedPath = IconUploadManager.uploadIconWithResize(iconFile, 32, 32);
                if (uploadedPath != null) {
                    selectedIconPath[0] = uploadedPath;
                    ImageIcon icon = IconUploadManager.getIconFromPath(uploadedPath);
                    if (icon != null) {
                        lblIconPreview.setIcon(icon);
                        lblIconPreview.setText("");
                    }
                    JOptionPane.showMessageDialog(dialog, 
                        "Icon uploaded successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        
        iconPanel.add(lblIconPreview);
        iconPanel.add(btnSelectIcon);
        panel.add(iconPanel, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnSave = new JButton("Save");
        btnSave.setBackground(new Color(76, 175, 80));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> {
            try {
                Marker marker = new Marker();
                marker.setMarkerName(txtName.getText());
                marker.setMarkerType((String) cboType.getSelectedItem());
                marker.setDescription(txtDescription.getText());
                marker.setLatitude(Double.parseDouble(txtLat.getText()));
                marker.setLongitude(Double.parseDouble(txtLng.getText()));
                marker.setIconPath(selectedIconPath[0]);
                marker.setCreatedBy(currentUserId);
                
                if (markerDAO.insertMarker(marker)) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Marker added successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadMarkers();
                    
                    // Auto-refresh MapFrame legend
                    com.mycompany.peta_usu.utils.MapRefreshUtil.refreshMarkers();
                } else {
                    JOptionPane.showMessageDialog(dialog, 
                        "Failed to add marker", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Invalid latitude or longitude", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dialog.dispose());
        
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        panel.add(btnPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void editSelectedMarker() {
        int selectedRow = markersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Please select a marker to edit", 
                "Info", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int markerId = (int) tableModel.getValueAt(selectedRow, 0);
        Marker marker = markerDAO.getMarkerById(markerId);
        
        if (marker != null) {
            // Edit dialog with pre-filled data
            JDialog editDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                "Edit Marker", true);
            editDialog.setSize(450, 400);
            editDialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // Nama
            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(new JLabel("Nama Gedung:"), gbc);
            gbc.gridx = 1;
            JTextField txtName = new JTextField(marker.getMarkerName(), 20);
            panel.add(txtName, gbc);
            
            // Tipe
            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(new JLabel("Tipe:"), gbc);
            gbc.gridx = 1;
            JComboBox<String> cboType = new JComboBox<>(new String[]{
                "Building", "Fakultas", "Gedung", "Fasilitas"
            });
            cboType.setSelectedItem(marker.getMarkerType());
            panel.add(cboType, gbc);
            
            // Latitude
            gbc.gridx = 0; gbc.gridy = 2;
            panel.add(new JLabel("Latitude:"), gbc);
            gbc.gridx = 1;
            JTextField txtLat = new JTextField(String.format("%.6f", marker.getLatitude()), 20);
            panel.add(txtLat, gbc);
            
            // Longitude
            gbc.gridx = 0; gbc.gridy = 3;
            panel.add(new JLabel("Longitude:"), gbc);
            gbc.gridx = 1;
            JTextField txtLng = new JTextField(String.format("%.6f", marker.getLongitude()), 20);
            panel.add(txtLng, gbc);
            
            // Deskripsi
            gbc.gridx = 0; gbc.gridy = 4;
            panel.add(new JLabel("Deskripsi:"), gbc);
            gbc.gridx = 1;
            JTextArea txtDescription = new JTextArea(
                marker.getDescription() != null ? marker.getDescription() : "", 4, 20);
            txtDescription.setLineWrap(true);
            txtDescription.setWrapStyleWord(true);
            JScrollPane scrollDesc = new JScrollPane(txtDescription);
            panel.add(scrollDesc, gbc);
            
            // Icon path (readonly)
            gbc.gridx = 0; gbc.gridy = 5;
            panel.add(new JLabel("Icon:"), gbc);
            gbc.gridx = 1;
            JLabel lblIcon = new JLabel(marker.getIconPath() != null ? 
                new File(marker.getIconPath()).getName() : "Default");
            panel.add(lblIcon, gbc);
            
            // Buttons
            gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            
            JButton btnUpdate = new JButton("Update");
            btnUpdate.setBackground(new Color(33, 150, 243));
            btnUpdate.setForeground(Color.WHITE);
            btnUpdate.addActionListener(e -> {
                try {
                    double lat = Double.parseDouble(txtLat.getText());
                    double lng = Double.parseDouble(txtLng.getText());
                    
                    // Validate coordinates are within USU area
                    if (lat < 3.55 || lat > 3.58 || lng < 98.65 || lng > 98.67) {
                        String message = String.format(
                            "<html><body style='width: 350px'>" +
                            "<h3>⚠️ Koordinat Di Luar Area USU!</h3>" +
                            "<p><b>Koordinat yang dimasukkan:</b><br>" +
                            "Latitude: %.6f<br>" +
                            "Longitude: %.6f</p>" +
                            "<p><b>Batas Area USU:</b><br>" +
                            "Latitude: 3.55 - 3.58<br>" +
                            "Longitude: 98.65 - 98.67</p>" +
                            "<p style='color: red'><b>⚠️ PERINGATAN:</b> Koordinat di luar area USU dapat " +
                            "menyebabkan masalah routing (rute jarak jauh/internasional)!</p>" +
                            "<p>Apakah Anda yakin ingin melanjutkan?</p>" +
                            "</body></html>",
                            lat, lng
                        );
                        
                        int confirm = JOptionPane.showConfirmDialog(editDialog,
                            message,
                            "Koordinat Di Luar Area USU",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                        
                        if (confirm != JOptionPane.YES_OPTION) {
                            return;
                        }
                    }
                    
                    marker.setMarkerName(txtName.getText().trim());
                    marker.setMarkerType((String) cboType.getSelectedItem());
                    marker.setLatitude(lat);
                    marker.setLongitude(lng);
                    marker.setDescription(txtDescription.getText().trim());
                    
                    if (markerDAO.updateMarker(marker)) {
                        String successMessage = String.format(
                            "<html><b>✅ Marker berhasil diupdate!</b><br><br>" +
                            "Nama: %s<br>" +
                            "Koordinat: %.6f, %.6f</html>",
                            marker.getMarkerName(), marker.getLatitude(), marker.getLongitude()
                        );
                        JOptionPane.showMessageDialog(editDialog, 
                            successMessage, 
                            "Sukses", 
                            JOptionPane.INFORMATION_MESSAGE);
                        editDialog.dispose();
                        loadMarkers();
                    } else {
                        JOptionPane.showMessageDialog(editDialog, 
                            "<html><b>❌ Gagal mengupdate marker!</b><br><br>" +
                            "Silakan coba lagi atau periksa log error.</html>", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(editDialog, 
                        "Invalid coordinates. Please enter valid numbers.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            });
            
            JButton btnCancel = new JButton("Cancel");
            btnCancel.addActionListener(e -> editDialog.dispose());
            
            btnPanel.add(btnUpdate);
            btnPanel.add(btnCancel);
            panel.add(btnPanel, gbc);
            
            editDialog.add(panel);
            editDialog.setVisible(true);
        }
    }
    
    private void deleteSelectedMarker() {
        int selectedRow = markersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Please select a marker to delete", 
                "Info", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this marker?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            int markerId = (int) tableModel.getValueAt(selectedRow, 0);
            
            if (markerDAO.deleteMarker(markerId)) {
                JOptionPane.showMessageDialog(this, 
                    "Marker deleted successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadMarkers();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to delete marker", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Show dialog to add marker at specific coordinates (dari right-click map)
     */
    private void showAddMarkerDialogAt(double latitude, double longitude) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Add Marker at Map Position", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Marker Name:"), gbc);
        gbc.gridx = 1;
        JTextField txtName = new JTextField(20);
        panel.add(txtName, gbc);
        
        // Type
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> cboType = new JComboBox<>(new String[]{
            "Building", "Fakultas", "Gedung", "Fasilitas"
        });
        panel.add(cboType, gbc);
        
        // Latitude (pre-filled)
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Latitude:"), gbc);
        gbc.gridx = 1;
        JTextField txtLat = new JTextField(String.format("%.6f", latitude), 20);
        panel.add(txtLat, gbc);
        
        // Longitude (pre-filled)
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Longitude:"), gbc);
        gbc.gridx = 1;
        JTextField txtLng = new JTextField(String.format("%.6f", longitude), 20);
        panel.add(txtLng, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        JTextArea txtDesc = new JTextArea(3, 20);
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        panel.add(scrollDesc, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnSave = new JButton("Save Marker");
        btnSave.setBackground(new Color(76, 175, 80));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> {
            try {
                String name = txtName.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Marker name cannot be empty!", 
                        "Validation Error", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                double lat = Double.parseDouble(txtLat.getText());
                double lng = Double.parseDouble(txtLng.getText());
                
                // VALIDASI KOORDINAT USU (PENTING!)
                if (lat < 3.55 || lat > 3.58 || lng < 98.65 || lng > 98.67) {
                    int confirm = JOptionPane.showConfirmDialog(dialog,
                        String.format(
                            "<html><body style='width: 300px;'>" +
                            "<h3 style='color: red;'>⚠️ Koordinat Di Luar Area USU!</h3>" +
                            "<p><b>Latitude:</b> %.6f (Valid: 3.55 - 3.58)</p>" +
                            "<p><b>Longitude:</b> %.6f (Valid: 98.65 - 98.67)</p>" +
                            "<p><b>Area USU Medan:</b><br>" +
                            "Lat: 3.55° - 3.58° N<br>" +
                            "Lng: 98.65° - 98.67° E</p>" +
                            "<p style='color: #d32f2f;'><b>Koordinat ini akan menyebabkan rute Google Maps error!</b></p>" +
                            "<p>Apakah Anda yakin ingin menyimpan koordinat di luar area USU?</p>" +
                            "</body></html>",
                            lat, lng
                        ),
                        "Koordinat Di Luar Area USU",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );
                    
                    if (confirm != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                
                Marker marker = new Marker();
                marker.setMarkerName(name);
                marker.setMarkerType((String) cboType.getSelectedItem());
                marker.setLatitude(lat);
                marker.setLongitude(lng);
                marker.setDescription(txtDesc.getText().trim());
                marker.setCreatedBy(currentUserId);
                
                if (markerDAO.insertMarker(marker)) {
                    JOptionPane.showMessageDialog(dialog, 
                        String.format(
                            "<html><body style='width: 250px;'>" +
                            "<h3 style='color: green;'>✅ Marker Berhasil Ditambahkan!</h3>" +
                            "<p><b>Nama:</b> %s</p>" +
                            "<p><b>Koordinat:</b><br>Lat: %.6f<br>Lng: %.6f</p>" +
                            "<p style='color: #0066cc;'><i>Marker dapat digunakan untuk routing</i></p>" +
                            "</body></html>",
                            name, lat, lng
                        ),
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadMarkers();
                    
                    // Auto-refresh MapFrame legend
                    com.mycompany.peta_usu.utils.MapRefreshUtil.refreshMarkers();
                } else {
                    JOptionPane.showMessageDialog(dialog, 
                        "Failed to add marker to database", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "<html><body style='width: 250px;'>" +
                    "<h3 style='color: red;'>❌ Format Koordinat Salah!</h3>" +
                    "<p>Koordinat harus berupa angka desimal.</p>" +
                    "<p><b>Contoh format yang benar:</b><br>" +
                    "Latitude: 3.569300<br>" +
                    "Longitude: 98.656400</p>" +
                    "</body></html>", 
                    "Invalid Format", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dialog.dispose());
        
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        panel.add(btnPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    /**
     * Upload icon dan add sebagai draggable marker
     */
    private void uploadAndAddIconMarker() {
        // File chooser untuk upload icon
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".png") 
                    || f.getName().toLowerCase().endsWith(".jpg")
                    || f.getName().toLowerCase().endsWith(".gif");
            }
            public String getDescription() {
                return "Image Files (*.png, *.jpg, *.gif)";
            }
        });
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Dialog untuk input nama dan deskripsi
            JDialog inputDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                "Add Building/Marker", true);
            inputDialog.setSize(400, 300);
            inputDialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // Nama Gedung
            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(new JLabel("Nama Gedung:"), gbc);
            gbc.gridx = 1;
            JTextField txtName = new JTextField(20);
            panel.add(txtName, gbc);
            
            // Tipe
            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(new JLabel("Tipe:"), gbc);
            gbc.gridx = 1;
            JComboBox<String> cboType = new JComboBox<>(new String[]{
                "Building", "Fakultas", "Gedung", "Fasilitas"
            });
            panel.add(cboType, gbc);
            
            // Deskripsi Gedung
            gbc.gridx = 0; gbc.gridy = 2;
            panel.add(new JLabel("Deskripsi:"), gbc);
            gbc.gridx = 1;
            JTextArea txtDescription = new JTextArea(4, 20);
            txtDescription.setLineWrap(true);
            txtDescription.setWrapStyleWord(true);
            JScrollPane scrollDesc = new JScrollPane(txtDescription);
            panel.add(scrollDesc, gbc);
            
            // Buttons
            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            
            JButton btnSave = new JButton("Upload & Add to Map");
            btnSave.setBackground(new Color(33, 150, 243));
            btnSave.setForeground(Color.WHITE);
            btnSave.addActionListener(e -> {
                String name = txtName.getText().trim();
                String desc = txtDescription.getText().trim();
                
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(inputDialog, 
                        "Nama gedung tidak boleh kosong!", 
                        "Validation Error", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                try {
                    // Upload icon
                    String iconPath = IconUploadManager.uploadIcon(selectedFile);
                    
                    // Create marker at center of map
                    GeoPosition centerPos = mapViewer.getCenterPosition();
                    
                    // Validate coordinates in USU area
                    if (!isWithinUSUArea(centerPos.getLatitude(), centerPos.getLongitude())) {
                        JOptionPane.showMessageDialog(inputDialog,
                            "Marker harus berada di dalam area USU!\nDrag marker ke dalam kampus USU.",
                            "Outside USU Area",
                            JOptionPane.WARNING_MESSAGE);
                        // Still allow but warn
                    }
                    
                    Marker marker = new Marker();
                    marker.setMarkerName(name);
                    marker.setMarkerType((String) cboType.getSelectedItem());
                    marker.setLatitude(centerPos.getLatitude());
                    marker.setLongitude(centerPos.getLongitude());
                    marker.setDescription(desc.isEmpty() ? "Drag to position" : desc);
                    marker.setIconPath(iconPath);
                    marker.setIconName(selectedFile.getName());
                    marker.setCreatedBy(currentUserId);
                    
                    if (markerDAO.insertMarker(marker)) {
                        JOptionPane.showMessageDialog(inputDialog,
                            "Icon uploaded! Drag marker to position gedung di map.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        inputDialog.dispose();
                        loadMarkers();
                        
                        // Auto-refresh MapFrame legend
                        com.mycompany.peta_usu.utils.MapRefreshUtil.refreshMarkers();
                    } else {
                        JOptionPane.showMessageDialog(inputDialog,
                            "Failed to add marker",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(inputDialog,
                        "Error uploading icon: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            });
            
            JButton btnCancel = new JButton("Cancel");
            btnCancel.addActionListener(e -> inputDialog.dispose());
            
            btnPanel.add(btnSave);
            btnPanel.add(btnCancel);
            panel.add(btnPanel, gbc);
            
            inputDialog.add(panel);
            inputDialog.setVisible(true);
        }
    }
    
    /**
     * Check if coordinates are within USU area
     */
    private boolean isWithinUSUArea(double lat, double lng) {
        // USU Campus approximate bounds
        double minLat = 3.555;
        double maxLat = 3.575;
        double minLng = 98.650;
        double maxLng = 98.675;
        
        return lat >= minLat && lat <= maxLat && lng >= minLng && lng <= maxLng;
    }
    
    /**
     * Save marker position after drag
     */
    private void saveMarkerPosition(DraggableWaypoint waypoint) {
        try {
            Marker marker = markerDAO.getMarkerById(waypoint.getMarkerId());
            if (marker != null) {
                marker.setLatitude(waypoint.getPosition().getLatitude());
                marker.setLongitude(waypoint.getPosition().getLongitude());
                
                if (markerDAO.updateMarker(marker)) {
                    System.out.println("Marker position saved: " + marker.getMarkerName() + 
                        " at " + marker.getLatitude() + ", " + marker.getLongitude());
                    loadMarkers(); // Refresh table
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * DraggableWaypoint class dengan icon custom
     */
    private static class DraggableWaypoint extends DefaultWaypoint {
        private final int markerId;
        private final String name;
        private final String iconPath;
        private GeoPosition position;
        
        public DraggableWaypoint(int markerId, String name, GeoPosition position, String iconPath) {
            super(position);
            this.markerId = markerId;
            this.name = name;
            this.position = position;
            this.iconPath = iconPath;
        }
        
        public int getMarkerId() {
            return markerId;
        }
        
        public String getName() {
            return name;
        }
        
        public String getIconPath() {
            return iconPath;
        }
        
        @Override
        public GeoPosition getPosition() {
            return position;
        }
        
        public void setPosition(GeoPosition position) {
            this.position = position;
        }
    }
    
    /**
     * Custom painter untuk draggable waypoints dengan icon
     */
    private class DraggableWaypointPainter implements Painter<JXMapViewer> {
        private Set<DraggableWaypoint> waypoints;
        
        public void setWaypoints(Set<DraggableWaypoint> waypoints) {
            this.waypoints = waypoints;
        }
        
        @Override
        public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
            if (waypoints == null) return;
            
            g = (Graphics2D) g.create();
            
            // Convert to world bitmap
            Rectangle rect = map.getViewportBounds();
            g.translate(-rect.x, -rect.y);
            
            for (DraggableWaypoint wp : waypoints) {
                Point2D point = map.getTileFactory().geoToPixel(
                    wp.getPosition(), map.getZoom());
                
                int x = (int) point.getX();
                int y = (int) point.getY();
                
                // Calculate icon size based on zoom level (1-15)
                // Zoom 1 (zoomed out): small icons, Zoom 15 (zoomed in): large icons
                int zoom = map.getZoom();
                double zoomFactor = Math.max(0.2, Math.min(1.0, (16 - zoom) / 10.0)); // 0.2 to 1.0
                int iconSize = (int)(28 * zoomFactor);
                int iconHalf = iconSize / 2;
                
                // Draw icon or default marker
                if (wp.getIconPath() != null && !wp.getIconPath().isEmpty()) {
                    try {
                        File iconFile = new File(wp.getIconPath());
                        if (iconFile.exists()) {
                            Image icon = ImageIO.read(iconFile);
                            g.drawImage(icon, x - iconHalf, y - iconSize, iconSize, iconSize, null);
                        } else {
                            drawDefaultMarker(g, x, y, wp, zoomFactor);
                        }
                    } catch (Exception e) {
                        drawDefaultMarker(g, x, y, wp, zoomFactor);
                    }
                } else {
                    drawDefaultMarker(g, x, y, wp, zoomFactor);
                }
                
                // Draw label with scaled font
                int fontSize = Math.max(8, (int)(10 * zoomFactor));
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, fontSize));
                FontMetrics fm = g.getFontMetrics();
                int textWidth = fm.stringWidth(wp.getName());
                g.fillRect(x - textWidth/2 - 2, y + 5, textWidth + 4, fm.getHeight());
                g.setColor(Color.WHITE);
                g.drawString(wp.getName(), x - textWidth/2, y + 5 + fm.getAscent());
            }
            
            g.dispose();
        }
        
        private void drawDefaultMarker(Graphics2D g, int x, int y, DraggableWaypoint wp, double zoomFactor) {
            // Scale marker size based on zoom
            int pinSize = (int)(16 * zoomFactor);
            int innerSize = (int)(10 * zoomFactor);
            int pointerSize = (int)(6 * zoomFactor);
            
            // Draw pin marker
            g.setColor(new Color(220, 50, 50));
            g.fillOval(x - pinSize/2, y - pinSize, pinSize, pinSize);
            g.setColor(Color.WHITE);
            g.fillOval(x - innerSize/2, y - pinSize + (pinSize - innerSize)/2, innerSize, innerSize);
            
            // Draw pointer
            int[] xPoints = {x, x - pointerSize, x + pointerSize};
            int[] yPoints = {y, y - pointerSize, y - pointerSize};
            g.setColor(new Color(220, 50, 50));
            g.fillPolygon(xPoints, yPoints, 3);
        }
    }
}
