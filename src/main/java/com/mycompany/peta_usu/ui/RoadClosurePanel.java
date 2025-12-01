package com.mycompany.peta_usu.ui;

import com.mycompany.peta_usu.dao.RoadClosureDAO;
import com.mycompany.peta_usu.dao.RoadDAO;
import com.mycompany.peta_usu.models.RoadClosure;
import com.mycompany.peta_usu.models.Road;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.*;
import java.sql.Date;
import java.util.List;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.*;

/**
 * RoadClosurePanel - Panel untuk admin manage road closures
 * 
 * @author PETA_USU Team
 */
public class RoadClosurePanel extends JPanel {
    
    private RoadClosureDAO closureDAO;
    private RoadDAO roadDAO;
    private int currentUserId;
    
    private JTable closuresTable;
    private DefaultTableModel tableModel;
    private JButton btnAddClosure;
    private JButton btnEditClosure;
    private JButton btnDeleteClosure;
    private JButton btnRefresh;
    private JComboBox<String> cboClosureType;
    
    // Map components
    private JXMapViewer mapViewer;
    private List<Road> allRoads;
    private List<RoadClosure> currentClosures;
    
    public RoadClosurePanel(int userId) {
        this.currentUserId = userId;
        this.closureDAO = new RoadClosureDAO();
        this.roadDAO = new RoadDAO();
        this.allRoads = roadDAO.getAllRoads();
        this.currentClosures = closureDAO.getActiveClosures();
        
        initComponents();
        loadClosures();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.setBackground(new Color(56, 136, 96));
        
        JLabel lblTitle = new JLabel("Manajemen Penutupan Jalan");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        topPanel.add(lblTitle);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Split pane: Map on left, Table on right
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);
        splitPane.setLeftComponent(createMapPanel());
        splitPane.setRightComponent(createTablePanel());
        
        add(splitPane, BorderLayout.CENTER);
        
        // Bottom - Info
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel("🔴 Merah = Tertutup Permanen  🟠 Orange = Tertutup Sementara  🔵 Biru = Satu Arah  ⚫ Hitam = Normal");
        bottomPanel.add(infoLabel);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createMapPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Peta Jalan Tertutup"));
        
        // Create map viewer with Google Maps
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
        
        mapViewer = new JXMapViewer();
        mapViewer.setTileFactory(tileFactory);
        
        // Set center to USU Campus (Medan)
        GeoPosition centerUSU = new GeoPosition(3.5678, 98.6532);
        mapViewer.setAddressLocation(centerUSU);
        mapViewer.setZoom(5);
        
        // Mouse listeners for pan and zoom
        PanMouseInputListener panListener = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(panListener);
        mapViewer.addMouseMotionListener(panListener);
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        
        // Add road painter to show roads with closure status
        mapViewer.setOverlayPainter(createRoadPainter());
        
        panel.add(mapViewer, BorderLayout.CENTER);
        
        return panel;
    }
    
    private Painter<JXMapViewer> createRoadPainter() {
        return new Painter<JXMapViewer>() {
            @Override
            public void paint(Graphics2D g, JXMapViewer map, int width, int height) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw all roads with closure status colors
                for (Road road : allRoads) {
                    // Determine color based on closure status
                    Color roadColor = getRoadColor(road);
                    g.setColor(roadColor);
                    g.setStroke(new BasicStroke(4.0f));
                    
                    // Convert geo coordinates to screen coordinates
                    GeoPosition start = new GeoPosition(road.getStartLat(), road.getStartLng());
                    GeoPosition end = new GeoPosition(road.getEndLat(), road.getEndLng());
                    
                    Point2D startPoint = map.getTileFactory().geoToPixel(start, map.getZoom());
                    Point2D endPoint = map.getTileFactory().geoToPixel(end, map.getZoom());
                    
                    Rectangle viewportBounds = map.getViewportBounds();
                    
                    int x1 = (int) (startPoint.getX() - viewportBounds.getX());
                    int y1 = (int) (startPoint.getY() - viewportBounds.getY());
                    int x2 = (int) (endPoint.getX() - viewportBounds.getX());
                    int y2 = (int) (endPoint.getY() - viewportBounds.getY());
                    
                    // Draw road line
                    g.drawLine(x1, y1, x2, y2);
                    
                    // Draw arrow for one-way streets
                    if (isOneWay(road)) {
                        drawArrow(g, x1, y1, x2, y2);
                    }
                }
            }
        };
    }
    
    private Color getRoadColor(Road road) {
        // Check if road has active closure
        for (RoadClosure closure : currentClosures) {
            if (closure.getRoadId() == road.getRoadId() && closure.isActive()) {
                switch (closure.getClosureType()) {
                    case PERMANENT:
                        return new Color(220, 20, 60); // Red
                    case TEMPORARY:
                        return new Color(255, 165, 0); // Orange
                    case ONE_WAY:
                        return new Color(30, 144, 255); // Blue
                }
            }
        }
        // Default: normal road (black)
        return Color.BLACK;
    }
    
    private boolean isOneWay(Road road) {
        for (RoadClosure closure : currentClosures) {
            if (closure.getRoadId() == road.getRoadId() && 
                closure.getClosureType() == RoadClosure.ClosureType.ONE_WAY &&
                closure.isActive()) {
                return true;
            }
        }
        return false;
    }
    
    private void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        
        int arrowSize = 10;
        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;
        
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        
        xPoints[0] = midX;
        yPoints[0] = midY;
        xPoints[1] = (int) (midX - arrowSize * Math.cos(angle - Math.PI / 6));
        yPoints[1] = (int) (midY - arrowSize * Math.sin(angle - Math.PI / 6));
        xPoints[2] = (int) (midX - arrowSize * Math.cos(angle + Math.PI / 6));
        yPoints[2] = (int) (midY - arrowSize * Math.sin(angle + Math.PI / 6));
        
        g.fillPolygon(xPoints, yPoints, 3);
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter Tipe:"));
        
        cboClosureType = new JComboBox<>(new String[]{
            "Semua", "Sementara", "Permanen", "Satu Arah"
        });
        cboClosureType.addActionListener(e -> filterClosures());
        filterPanel.add(cboClosureType);
        
        btnRefresh = new JButton("Muat Ulang");
        btnRefresh.addActionListener(e -> loadClosures());
        filterPanel.add(btnRefresh);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "Nama Jalan", "Tipe", "Alasan", "Tanggal Mulai", "Tanggal Selesai", "Aktif"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        closuresTable = new JTable(tableModel);
        closuresTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(closuresTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        btnAddClosure = new JButton("Tambah Penutupan");
        btnAddClosure.setBackground(new Color(76, 175, 80));
        btnAddClosure.setForeground(Color.WHITE);
        btnAddClosure.addActionListener(e -> showAddClosureDialog());
        
        btnEditClosure = new JButton("Edit");
        btnEditClosure.addActionListener(e -> editSelectedClosure());
        
        btnDeleteClosure = new JButton("Hapus");
        btnDeleteClosure.setBackground(new Color(244, 67, 54));
        btnDeleteClosure.setForeground(Color.WHITE);
        btnDeleteClosure.addActionListener(e -> deleteSelectedClosure());
        
        buttonsPanel.add(btnAddClosure);
        buttonsPanel.add(btnEditClosure);
        buttonsPanel.add(btnDeleteClosure);
        
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadClosures() {
        tableModel.setRowCount(0);
        currentClosures = closureDAO.getActiveClosures();
        
        for (RoadClosure closure : currentClosures) {
            tableModel.addRow(new Object[]{
                closure.getClosureId(),
                closure.getRoadName(),
                closure.getClosureType().getValue(),
                closure.getReason(),
                closure.getStartDate(),
                closure.getEndDate(),
                closure.isActive() ? "Ya" : "Tidak"
            });
        }
        
        // Refresh map to show updated closure status
        if (mapViewer != null) {
            mapViewer.repaint();
        }
    }
    
    private void filterClosures() {
        String selected = (String) cboClosureType.getSelectedItem();
        
        if ("Semua".equals(selected)) {
            loadClosures();
            return;
        }
        
        tableModel.setRowCount(0);
        RoadClosure.ClosureType type = null;
        
        switch (selected) {
            case "Sementara":
                type = RoadClosure.ClosureType.TEMPORARY;
                break;
            case "Permanen":
                type = RoadClosure.ClosureType.PERMANENT;
                break;
            case "Satu Arah":
                type = RoadClosure.ClosureType.ONE_WAY;
                break;
        }
        
        if (type != null) {
            currentClosures = closureDAO.getClosuresByType(type);
            for (RoadClosure closure : currentClosures) {
                tableModel.addRow(new Object[]{
                    closure.getClosureId(),
                    closure.getRoadName(),
                    closure.getClosureType().getValue(),
                    closure.getReason(),
                    closure.getStartDate(),
                    closure.getEndDate(),
                    closure.isActive() ? "Ya" : "Tidak"
                });
            }
        }
        
        // Refresh map
        if (mapViewer != null) {
            mapViewer.repaint();
        }
    }
    
    private void showAddClosureDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Tambah Penutupan Jalan", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Nama Jalan (dropdown)
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nama Jalan:"), gbc);
        
        gbc.gridx = 1;
        List<Road> roads = roadDAO.getAllRoads();
        JComboBox<Road> cboRoad = new JComboBox<>();
        for (Road road : roads) {
            cboRoad.addItem(road);
        }
        panel.add(cboRoad, gbc);
        
        // Closure Type
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Tipe:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<String> cboType = new JComboBox<>(new String[]{
            "Sementara", "Permanen", "Satu Arah"
        });
        panel.add(cboType, gbc);
        
        // Reason
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Alasan:"), gbc);
        
        gbc.gridx = 1;
        JTextArea txtReason = new JTextArea(3, 20);
        txtReason.setLineWrap(true);
        panel.add(new JScrollPane(txtReason), gbc);
        
        // Start Date
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Tanggal Mulai:"), gbc);
        
        gbc.gridx = 1;
        JTextField txtStartDate = new JTextField("2025-12-01");
        panel.add(txtStartDate, gbc);
        
        // End Date
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Tanggal Selesai:"), gbc);
        
        gbc.gridx = 1;
        JTextField txtEndDate = new JTextField("2025-12-31");
        panel.add(txtEndDate, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnSave = new JButton("Simpan");
        btnSave.setBackground(new Color(76, 175, 80));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> {
            try {
                Road selectedRoad = (Road) cboRoad.getSelectedItem();
                if (selectedRoad == null) {
                    JOptionPane.showMessageDialog(dialog, "Pilih jalan terlebih dahulu!");
                    return;
                }
                
                RoadClosure closure = new RoadClosure();
                closure.setRoadId(selectedRoad.getRoadId());
                
                String type = (String) cboType.getSelectedItem();
                RoadClosure.ClosureType closureType;
                switch (type) {
                    case "Sementara":
                        closureType = RoadClosure.ClosureType.TEMPORARY;
                        break;
                    case "Permanen":
                        closureType = RoadClosure.ClosureType.PERMANENT;
                        break;
                    case "Satu Arah":
                        closureType = RoadClosure.ClosureType.ONE_WAY;
                        break;
                    default:
                        closureType = RoadClosure.ClosureType.TEMPORARY;
                }
                closure.setClosureType(closureType);
                
                closure.setReason(txtReason.getText());
                closure.setStartDate(Date.valueOf(txtStartDate.getText()));
                closure.setEndDate(Date.valueOf(txtEndDate.getText()));
                closure.setCreatedBy(currentUserId);
                
                if (closureDAO.insertClosure(closure)) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Penutupan jalan berhasil ditambahkan!", 
                        "Sukses", 
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadClosures();
                } else {
                    JOptionPane.showMessageDialog(dialog, 
                        "Gagal menambahkan penutupan jalan", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Input tidak valid: " + ex.getMessage(), 
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
    
    private void editSelectedClosure() {
        int selectedRow = closuresTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Pilih penutupan jalan yang akan diedit", 
                "Info", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JOptionPane.showMessageDialog(this, 
            "Fitur edit akan segera ditambahkan", 
            "Info", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void deleteSelectedClosure() {
        int selectedRow = closuresTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Pilih penutupan jalan yang akan dihapus", 
                "Info", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Apakah Anda yakin ingin menghapus penutupan jalan ini?", 
            "Konfirmasi Hapus", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            int closureId = (int) tableModel.getValueAt(selectedRow, 0);
            
            if (closureDAO.deleteClosure(closureId)) {
                JOptionPane.showMessageDialog(this, 
                    "Penutupan jalan berhasil dihapus!", 
                    "Sukses", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadClosures();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Gagal menghapus penutupan jalan", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
