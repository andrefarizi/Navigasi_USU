package com.mycompany.peta_usu.ui;

import com.mycompany.peta_usu.dao.BuildingDAO;
import com.mycompany.peta_usu.dao.MarkerDAO;
import com.mycompany.peta_usu.models.Building;
import com.mycompany.peta_usu.models.Marker;
import com.mycompany.peta_usu.utils.GoogleMapsHelper;
import com.mycompany.peta_usu.utils.GoogleMapsHelper.MapMarker;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * UserMapFrame - Map view untuk user (non-admin)
 * Menampilkan peta USU dengan markers dan building info
 * 
 * === 4 PILAR OOP ===
 * 1. ENCAPSULATION: Field buildingDAO, allBuildings, searchField PRIVATE
 * 2. INHERITANCE: Extends JFrame (parent: javax.swing.JFrame)
 *    Mewarisi method dari JFrame:
 *    • setTitle(), setSize(), setDefaultCloseOperation()
 *    • setLocationRelativeTo(), setVisible(), add(), dispose()
 *    - Implements DocumentListener (parent: javax.swing.event.DocumentListener)
 *      Method yang harus diimplement: insertUpdate(), removeUpdate(), changedUpdate()
 * 3. POLYMORPHISM: Override insertUpdate/removeUpdate untuk search
 * 4. ABSTRACTION: Method loadBuildings() sembunyikan DAO query + UI update
 * 
 * @author PETA_USU Team
 */
public class UserMapFrame extends JFrame {  // ← INHERITANCE dari javax.swing.JFrame
    
    // ========== ENCAPSULATION: Field PRIVATE ==========
    private BuildingDAO buildingDAO;        // ← PRIVATE: Database access
    private List<Building> allBuildings;    // ← PRIVATE: Data cache
    
    private JEditorPane mapDisplay;         // ← PRIVATE: UI component
    private JTextField searchField;         // ← PRIVATE: Search input
    private JComboBox<String> buildingTypeFilter;  // ← PRIVATE: Filter dropdown
    private JList<String> buildingList;     // ← PRIVATE: List widget
    private DefaultListModel<String> listModel;    // ← PRIVATE: List data
    
    public UserMapFrame() {
        this.buildingDAO = new BuildingDAO();
        this.allBuildings = new ArrayList<>();
        
        setTitle("PetaUSU - Campus Navigation");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);
        
        initComponents();
        loadBuildings();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Main content - split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createSidebarPanel());
        splitPane.setRightComponent(createMapPanel());
        splitPane.setDividerLocation(350);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Footer
        add(createFooterPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(56, 136, 96));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("PetaUSU - Navigasi Kampus USU");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Universitas Sumatera Utara");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(220, 255, 220));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        // Info button
        JButton btnInfo = new JButton("ℹ About");
        btnInfo.setBackground(new Color(33, 150, 243));
        btnInfo.setForeground(Color.WHITE);
        btnInfo.addActionListener(e -> showAboutDialog());
        
        panel.add(btnInfo, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createSidebarPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Search and filter panel
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        
        // Search field
        searchField = new JTextField();
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filterBuildings(); }
            public void removeUpdate(DocumentEvent e) { filterBuildings(); }
            public void insertUpdate(DocumentEvent e) { filterBuildings(); }
        });
        
        JPanel searchFieldPanel = new JPanel(new BorderLayout());
        searchFieldPanel.add(new JLabel("🔍 Search: "), BorderLayout.WEST);
        searchFieldPanel.add(searchField, BorderLayout.CENTER);
        
        searchPanel.add(searchFieldPanel, BorderLayout.NORTH);
        
        // Type filter
        buildingTypeFilter = new JComboBox<>(new String[]{
            "All Types", "Academic", "Laboratory", "Office", "Library", 
            "Mosque", "Sport Facility", "Other"
        });
        buildingTypeFilter.addActionListener(e -> filterBuildings());
        
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.add(new JLabel("Filter: "), BorderLayout.WEST);
        filterPanel.add(buildingTypeFilter, BorderLayout.CENTER);
        
        searchPanel.add(filterPanel, BorderLayout.SOUTH);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        
        // Building list
        listModel = new DefaultListModel<>();
        buildingList = new JList<>(listModel);
        buildingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        buildingList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedBuilding();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(buildingList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Buildings & Facilities"));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        
        JButton btnShowInfo = new JButton("📋 Show Details");
        btnShowInfo.addActionListener(e -> showBuildingDetails());
        
        JButton btnRefresh = new JButton("🔄 Refresh Map");
        btnRefresh.addActionListener(e -> refreshMap());
        
        buttonsPanel.add(btnShowInfo);
        buttonsPanel.add(btnRefresh);
        
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createMapPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Map display using HTML
        mapDisplay = new JEditorPane();
        mapDisplay.setContentType("text/html");
        mapDisplay.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(mapDisplay);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel lblFooter = new JLabel("© 2025 PetaUSU - Click on building name to view details");
        lblFooter.setFont(new Font("Arial", Font.PLAIN, 11));
        lblFooter.setForeground(Color.GRAY);
        
        panel.add(lblFooter);
        
        return panel;
    }
    
    private void loadBuildings() {
        allBuildings = buildingDAO.getAllBuildings();
        
        // Populate list
        for (Building building : allBuildings) {
            listModel.addElement(
                building.getBuildingCode() + " - " + building.getBuildingName()
            );
        }
        
        // Load map
        refreshMap();
    }
    
    private void filterBuildings() {
        String searchText = searchField.getText().toLowerCase();
        String selectedType = (String) buildingTypeFilter.getSelectedItem();
        
        listModel.clear();
        
        for (Building building : allBuildings) {
            // Filter by search text
            boolean matchesSearch = searchText.isEmpty() || 
                building.getBuildingName().toLowerCase().contains(searchText) ||
                building.getBuildingCode().toLowerCase().contains(searchText);
            
            // Filter by type
            boolean matchesType = "All Types".equals(selectedType) ||
                building.getBuildingType().getValue().equalsIgnoreCase(
                    selectedType.replace(" ", "_")
                );
            
            if (matchesSearch && matchesType) {
                listModel.addElement(
                    building.getBuildingCode() + " - " + building.getBuildingName()
                );
            }
        }
    }
    
    private void showSelectedBuilding() {
        String selected = buildingList.getSelectedValue();
        if (selected != null) {
            String code = selected.split(" - ")[0];
            
            // Find building
            Building building = allBuildings.stream()
                .filter(b -> b.getBuildingCode().equals(code))
                .findFirst()
                .orElse(null);
            
            if (building != null) {
                // Update map to center on building
                refreshMapWithFocus(building);
            }
        }
    }
    
    private void showBuildingDetails() {
        String selected = buildingList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a building first", 
                "Info", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String code = selected.split(" - ")[0];
        
        Building building = allBuildings.stream()
            .filter(b -> b.getBuildingCode().equals(code))
            .findFirst()
            .orElse(null);
        
        if (building != null) {
            BuildingInfoDialog.showBuildingInfo(this, building);
        }
    }
    
    private void refreshMap() {
        // Create map markers from buildings
        List<MapMarker> markers = new ArrayList<>();
        
        for (Building building : allBuildings) {
            MapMarker marker = new MapMarker(
                building.getLatitude(),
                building.getLongitude(),
                building.getBuildingName(),
                building.getBuildingCode() + "<br>" + building.getAddress()
            );
            markers.add(marker);
        }
        
        // Generate HTML map
        String mapHtml = GoogleMapsHelper.generateMapHTMLWithMarkers(markers);
        mapDisplay.setText(mapHtml);
    }
    
    private void refreshMapWithFocus(Building building) {
        List<MapMarker> markers = new ArrayList<>();
        
        for (Building b : allBuildings) {
            MapMarker marker = new MapMarker(
                b.getLatitude(),
                b.getLongitude(),
                b.getBuildingName(),
                b.getBuildingCode() + "<br>" + b.getAddress()
            );
            markers.add(marker);
        }
        
        // Generate HTML with focus on selected building
        String mapHtml = GoogleMapsHelper.generateMapHTMLWithMarkers(
            building.getLatitude(),
            building.getLongitude(),
            17, // Zoom level
            markers
        );
        
        mapDisplay.setText(mapHtml);
    }
    
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "PetaUSU - Navigasi Kampus USU\n\n" +
            "Sistem navigasi berbasis peta untuk\n" +
            "Universitas Sumatera Utara\n\n" +
            "Fitur:\n" +
            "• Peta interaktif gedung kampus\n" +
            "• Informasi detail building dan ruangan\n" +
            "• Pencarian gedung\n" +
            "• Filter berdasarkan tipe\n\n" +
            "Version 1.0\n" +
            "© 2025 PETA_USU Team",
            "About PetaUSU",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Launch user map frame
     */
    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            UserMapFrame frame = new UserMapFrame();
            frame.setVisible(true);
        });
    }
    
    /**
     * Test main
     */
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        launch();
    }
}
