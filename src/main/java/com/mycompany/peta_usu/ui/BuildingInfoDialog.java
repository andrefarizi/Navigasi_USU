package com.mycompany.peta_usu.ui;

import com.mycompany.peta_usu.dao.BuildingDAO;
import com.mycompany.peta_usu.dao.RoomDAO;
import com.mycompany.peta_usu.models.Building;
import com.mycompany.peta_usu.models.Room;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * BuildingInfoDialog - Dialog untuk tampilkan info building
 * Untuk user melihat detail gedung dan ruangan
 * 
 * === 4 PILAR OOP ===
 * 1. ENCAPSULATION: Field building, buildingDAO, roomDAO PRIVATE
 * 2. INHERITANCE: Extends JDialog (parent: javax.swing.JDialog)
 *    Mewarisi method dari JDialog:
 *    • setModal() - set dialog blocking behavior
 *    • setTitle() - set dialog title
 *    • setSize() - set dialog size
 *    • setLocationRelativeTo() - center dialog
 *    • dispose() - close dialog
 *    • add() - add component
 * 3. POLYMORPHISM: Constructor overloading (by ID vs by object)
 * 4. ABSTRACTION: Method createRoomsPanel() sembunyikan table creation
 * 
 * @author PETA_USU Team
 */
public class BuildingInfoDialog extends JDialog {  // ← INHERITANCE dari javax.swing.JDialog
    
    // ========== ENCAPSULATION: Field PRIVATE ==========
    private Building building;          // ← PRIVATE: Data object
    private BuildingDAO buildingDAO;    // ← PRIVATE: Database access
    private RoomDAO roomDAO;            // ← PRIVATE: Database access            // ← PRIVATE: Database access
    
    // ========== POLYMORPHISM: Constructor Overloading ==========
    // Constructor 1: Terima building ID
    public BuildingInfoDialog(Frame parent, int buildingId) {  // ← POLYMORPHISM
        super(parent, "Building Information", true);
        
        this.buildingDAO = new BuildingDAO();
        this.roomDAO = new RoomDAO();
        this.building = buildingDAO.getBuildingById(buildingId);
        
        if (building != null) {
            initComponents();
        }
        
        setSize(600, 500);
        setLocationRelativeTo(parent);
    }
    
    // Constructor 2: Terima building object langsung
    public BuildingInfoDialog(Frame parent, Building building) {  // ← POLYMORPHISM
        super(parent, "Building Information", true);
        
        this.roomDAO = new RoomDAO();
        this.building = building;
        
        initComponents();
        
        setSize(600, 500);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content - tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Building Info", createInfoPanel());
        tabbedPane.addTab("Rooms/Classes", createRoomsPanel());
        tabbedPane.addTab("Facilities", createFacilitiesPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Bottom - Close button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());
        bottomPanel.add(btnClose);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(56, 136, 96));
        
        // Building name and code
        JLabel lblName = new JLabel(building.getBuildingName());
        lblName.setFont(new Font("Arial", Font.BOLD, 24));
        lblName.setForeground(Color.WHITE);
        
        JLabel lblCode = new JLabel(building.getBuildingCode() + " - " + 
            building.getBuildingType().getValue());
        lblCode.setFont(new Font("Arial", Font.PLAIN, 14));
        lblCode.setForeground(Color.WHITE);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(lblName);
        textPanel.add(lblCode);
        
        panel.add(textPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Building Code
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(createLabel("Building Code:", true), gbc);
        gbc.gridx = 1;
        panel.add(createLabel(building.getBuildingCode(), false), gbc);
        
        // Type
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(createLabel("Type:", true), gbc);
        gbc.gridx = 1;
        panel.add(createLabel(building.getBuildingType().getValue(), false), gbc);
        
        // Address
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(createLabel("Address:", true), gbc);
        gbc.gridx = 1;
        JTextArea txtAddress = new JTextArea(building.getAddress());
        txtAddress.setEditable(false);
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        txtAddress.setBackground(panel.getBackground());
        panel.add(txtAddress, gbc);
        
        // Floor Count
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(createLabel("Floor Count:", true), gbc);
        gbc.gridx = 1;
        panel.add(createLabel(String.valueOf(building.getFloorCount()), false), gbc);
        
        // Coordinates
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(createLabel("Coordinates:", true), gbc);
        gbc.gridx = 1;
        panel.add(createLabel(
            String.format("%.6f, %.6f", building.getLatitude(), building.getLongitude()), 
            false
        ), gbc);
        
        // Description
        if (building.getDescription() != null && !building.getDescription().isEmpty()) {
            gbc.gridx = 0; gbc.gridy = 5;
            gbc.gridwidth = 2;
            panel.add(createLabel("Description:", true), gbc);
            
            gbc.gridy = 6;
            JTextArea txtDesc = new JTextArea(building.getDescription());
            txtDesc.setEditable(false);
            txtDesc.setLineWrap(true);
            txtDesc.setWrapStyleWord(true);
            txtDesc.setRows(4);
            txtDesc.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            panel.add(new JScrollPane(txtDesc), gbc);
        }
        
        return panel;
    }
    
    private JPanel createRoomsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Get rooms for this building
        List<Room> rooms = roomDAO.getRoomsByBuilding(building.getBuildingId());
        
        if (rooms.isEmpty()) {
            JLabel lblEmpty = new JLabel("No rooms/classes data available");
            lblEmpty.setHorizontalAlignment(JLabel.CENTER);
            panel.add(lblEmpty, BorderLayout.CENTER);
        } else {
            // Create table
            String[] columns = {"Room Code", "Room Name", "Floor", "Type", "Capacity"};
            Object[][] data = new Object[rooms.size()][5];
            
            for (int i = 0; i < rooms.size(); i++) {
                Room room = rooms.get(i);
                data[i][0] = room.getRoomCode();
                data[i][1] = room.getRoomName();
                data[i][2] = room.getFloorNumber();
                data[i][3] = room.getRoomType().getValue();
                data[i][4] = room.getCapacity();
            }
            
            JTable table = new JTable(data, columns);
            table.setEnabled(false);
            
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            
            // Summary
            JLabel lblSummary = new JLabel("Total: " + rooms.size() + " rooms/classes");
            panel.add(lblSummary, BorderLayout.SOUTH);
        }
        
        return panel;
    }
    
    private JPanel createFacilitiesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextArea txtFacilities = new JTextArea();
        txtFacilities.setEditable(false);
        txtFacilities.setLineWrap(true);
        txtFacilities.setWrapStyleWord(true);
        
        // Example facilities
        txtFacilities.setText(
            "Facilities information:\n\n" +
            "• WiFi Available\n" +
            "• Air Conditioning\n" +
            "• Parking Area\n" +
            "• Accessible for Disabled\n" +
            "• Prayer Room\n" +
            "• Canteen/Food Court\n\n" +
            "Note: Facilities data will be loaded from database"
        );
        
        panel.add(new JScrollPane(txtFacilities), BorderLayout.CENTER);
        
        return panel;
    }
    
    private JLabel createLabel(String text, boolean bold) {
        JLabel label = new JLabel(text);
        if (bold) {
            label.setFont(label.getFont().deriveFont(Font.BOLD));
        }
        return label;
    }
    
    /**
     * Show building info dialog
     */
    public static void showBuildingInfo(Frame parent, int buildingId) {
        BuildingInfoDialog dialog = new BuildingInfoDialog(parent, buildingId);
        dialog.setVisible(true);
    }
    
    public static void showBuildingInfo(Frame parent, Building building) {
        BuildingInfoDialog dialog = new BuildingInfoDialog(parent, building);
        dialog.setVisible(true);
    }
}
