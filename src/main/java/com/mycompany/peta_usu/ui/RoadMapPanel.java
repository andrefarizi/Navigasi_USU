package com.mycompany.peta_usu.ui;

import com.mycompany.peta_usu.dao.*;
import com.mycompany.peta_usu.models.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.*;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.*;
import javax.swing.event.MouseInputListener;
import org.jxmapviewer.painter.CompoundPainter;
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
    
    // Data
    private List<Road> allRoads;
    private Map<Integer, RoadClosure> activeClosures;
    private Road selectedRoad = null;
    
    public RoadMapPanel(int userId) {
        this.currentUserId = userId;
        this.roadDAO = new RoadDAO();
        this.closureDAO = new RoadClosureDAO();
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
        
        // Initialize JXMapViewer
        mapViewer = new JXMapViewer();
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
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
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        btnAddRoad = new JButton("➕ Tambah Jalan");
        btnAddRoad.setBackground(new Color(76, 175, 80));
        btnAddRoad.setForeground(Color.WHITE);
        btnAddRoad.addActionListener(e -> showAddRoadDialog());
        
        btnEditRoad = new JButton("✏️ Edit");
        btnEditRoad.setBackground(new Color(33, 150, 243));
        btnEditRoad.setForeground(Color.WHITE);
        btnEditRoad.addActionListener(e -> editSelectedRoad());
        
        btnSetClosure = new JButton("🚧 Atur Penutupan");
        btnSetClosure.setBackground(new Color(255, 152, 0));
        btnSetClosure.setForeground(Color.WHITE);
        btnSetClosure.addActionListener(e -> setRoadClosure());
        
        btnDeleteRoad = new JButton("🗑️ Hapus");
        btnDeleteRoad.setBackground(new Color(244, 67, 54));
        btnDeleteRoad.setForeground(Color.WHITE);
        btnDeleteRoad.addActionListener(e -> deleteSelectedRoad());
        
        btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadRoads());
        
        buttonsPanel.add(btnAddRoad);
        buttonsPanel.add(btnEditRoad);
        buttonsPanel.add(btnSetClosure);
        buttonsPanel.add(btnDeleteRoad);
        buttonsPanel.add(btnRefresh);
        
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEtchedBorder());
        
        JLabel lblInfo = new JLabel("Klik jalan di peta untuk memilih | Klik 'Atur Penutupan' untuk mengubah status jalan");
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 12));
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
                    int strokeWidth = 3;
                    
                    if (activeClosures.containsKey(road.getRoadId())) {
                        RoadClosure closure = activeClosures.get(road.getRoadId());
                        if (closure.getClosureType() == RoadClosure.ClosureType.PERMANENT) {
                            roadColor = Color.RED;
                        } else if (closure.getClosureType() == RoadClosure.ClosureType.TEMPORARY) {
                            roadColor = new Color(255, 140, 0); // Orange
                        }
                    }
                    
                    // Determine stroke based on road type
                    BasicStroke stroke;
                    if ("ONE_WAY".equals(road.getRoadType())) {
                        roadColor = new Color(0, 100, 200); // Blue
                        stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, 
                            BasicStroke.JOIN_ROUND, 0, new float[]{10, 5}, 0);
                    } else if ("TWO_WAY".equals(road.getRoadType())) {
                        roadColor = new Color(0, 150, 0); // Green
                        stroke = new BasicStroke(strokeWidth);
                    } else {
                        stroke = new BasicStroke(strokeWidth);
                    }
                    
                    // Convert geo positions to screen points
                    GeoPosition start = new GeoPosition(road.getStartLat(), road.getStartLng());
                    GeoPosition end = new GeoPosition(road.getEndLat(), road.getEndLng());
                    
                    Point2D startPoint = map.getTileFactory().geoToPixel(start, map.getZoom());
                    Point2D endPoint = map.getTileFactory().geoToPixel(end, map.getZoom());
                    
                    Rectangle viewportBounds = map.getViewportBounds();
                    int x1 = (int)(startPoint.getX() - viewportBounds.getX());
                    int y1 = (int)(startPoint.getY() - viewportBounds.getY());
                    int x2 = (int)(endPoint.getX() - viewportBounds.getX());
                    int y2 = (int)(endPoint.getY() - viewportBounds.getY());
                    
                    // Draw road line
                    g.setColor(roadColor);
                    g.setStroke(stroke);
                    g.drawLine(x1, y1, x2, y2);
                    
                    // Draw arrow untuk one-way roads
                    if ("ONE_WAY".equals(road.getRoadType())) {
                        drawArrow(g, x1, y1, x2, y2, roadColor);
                    } else if ("TWO_WAY".equals(road.getRoadType())) {
                        // Draw double arrow
                        drawArrow(g, x1, y1, x2, y2, roadColor);
                        drawArrow(g, x2, y2, x1, y1, roadColor);
                    }
                    
                    // Highlight selected road
                    if (selectedRoad != null && selectedRoad.getRoadId() == road.getRoadId()) {
                        g.setColor(new Color(255, 255, 0, 150)); // Yellow highlight
                        g.setStroke(new BasicStroke(strokeWidth + 4));
                        g.drawLine(x1, y1, x2, y2);
                    }
                }
            }
        };
        
        mapViewer.setOverlayPainter(roadPainter);
        mapViewer.repaint();
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
        JOptionPane.showMessageDialog(this,
            "Fitur tambah jalan akan segera ditambahkan.\n" +
            "Untuk sementara, gunakan menu 'Jalan' di sidebar.",
            "Info",
            JOptionPane.INFORMATION_MESSAGE);
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
}
