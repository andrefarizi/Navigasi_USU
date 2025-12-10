package com.mycompany.peta_usu.ui;

import com.mycompany.peta_usu.dao.BuildingDAO;
import com.mycompany.peta_usu.models.Building;
import com.mycompany.peta_usu.utils.MapRefreshUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Panel CRUD untuk Buildings - Terintegrasi dengan Google Maps
 * 
 * === 4 PILAR OOP ===
 * 1. ENCAPSULATION: Field buildingDAO, table, textfield PRIVATE
 * 2. INHERITANCE: Extends JPanel (parent: javax.swing.JPanel)
 *    Mewarisi method dari JPanel:
 *    • setLayout() - set layout manager
 *    • add() - tambah component
 *    • setBackground() - set background color
 *    • revalidate(), repaint() - refresh UI
 * 3. POLYMORPHISM: Override isCellEditable() di DefaultTableModel
 * 4. ABSTRACTION: Method loadBuildings() sembunyikan SQL query
 */
public class BuildingManagementPanel extends JPanel {  // ← INHERITANCE dari javax.swing.JPanel
    // ========== ENCAPSULATION: Semua field PRIVATE ==========
    private BuildingDAO buildingDAO;              // ← PRIVATE: DAO
    private JTable buildingTable;                 // ← PRIVATE: UI
    private DefaultTableModel tableModel;         // ← PRIVATE: Data model
    private JTextField tfName, tfLat, tfLng, tfDescription;  // ← PRIVATE
    private JComboBox<Building.BuildingType> cbType;         // ← PRIVATE
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnRefresh;  // ← PRIVATE
    private int selectedBuildingId = -1;          // ← PRIVATE: State
    
    public BuildingManagementPanel() {
        buildingDAO = new BuildingDAO();
        initComponents();
        loadBuildings();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Title
        JLabel title = new JLabel("📁 Manajemen Gedung USU");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "Nama Gedung", "Tipe", "Deskripsi"};
        // ========== POLYMORPHISM: Override method ==========
        tableModel = new DefaultTableModel(columns, 0) {
            @Override  // ← POLYMORPHISM: Override isCellEditable()
            public boolean isCellEditable(int row, int column) {
                return false;  // Semua cell read-only
            }
        };
        buildingTable = new JTable(tableModel);
        buildingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        buildingTable.setRowHeight(25);
        buildingTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectRow();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(buildingTable);
        scrollPane.setPreferredSize(new Dimension(800, 300));
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Gedung"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Row 0 - Nama
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nama Gedung:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        tfName = new JTextField(30);
        formPanel.add(tfName, gbc);
        
        // Row 1 - Type
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Tipe:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cbType = new JComboBox<>(Building.BuildingType.values());
        formPanel.add(cbType, gbc);
        
        // Row 2 - Description (koordinat disembunyikan dari UI)
        tfLat = new JTextField(15);
        tfLat.setVisible(false);
        tfLng = new JTextField(15);
        tfLng.setVisible(false);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Deskripsi:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        tfDescription = new JTextField(30);
        formPanel.add(tfDescription, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        btnAdd = new JButton("➕ Tambah");
        btnAdd.setBackground(new Color(40, 167, 69));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.addActionListener(e -> addBuilding());
        
        btnUpdate = new JButton("✏️ Update");
        btnUpdate.setBackground(new Color(0, 123, 255));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setFocusPainted(false);
        btnUpdate.setEnabled(false);
        btnUpdate.addActionListener(e -> updateBuilding());
        
        btnDelete = new JButton("🗑️ Hapus");
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(e -> deleteBuilding());
        
        btnClear = new JButton("🔄 Bersihkan");
        btnClear.addActionListener(e -> clearForm());
        
        btnRefresh = new JButton("♻️ Muat Ulang");
        btnRefresh.addActionListener(e -> loadBuildings());
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnRefresh);
        
        // Center Panel
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(formPanel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadBuildings() {
        tableModel.setRowCount(0);
        List<Building> buildings = buildingDAO.getAllBuildings();
        for (Building b : buildings) {
            Object[] row = {
                b.getBuildingId(),
                b.getBuildingName(),
                b.getBuildingType().name(),
                b.getDescription()
            };
            tableModel.addRow(row);
        }
    }
    
    private void selectRow() {
        int row = buildingTable.getSelectedRow();
        if (row >= 0) {
            selectedBuildingId = (int) tableModel.getValueAt(row, 0);
            tfName.setText((String) tableModel.getValueAt(row, 1));
            String typeDisplay = (String) tableModel.getValueAt(row, 2);
            for (Building.BuildingType type : Building.BuildingType.values()) {
                if (type.name().equals(typeDisplay)) {
                    cbType.setSelectedItem(type);
                    break;
                }
            }
            // Set default coordinates (center of USU campus)
            tfLat.setText("3.5651891");
            tfLng.setText("98.6566015");
            tfDescription.setText((String) tableModel.getValueAt(row, 3));
            
            btnUpdate.setEnabled(true);
            btnDelete.setEnabled(true);
            btnAdd.setEnabled(false);
        }
    }
    
    private void addBuilding() {
        try {
            Building building = new Building();
            building.setBuildingName(tfName.getText().trim());
            building.setBuildingType((Building.BuildingType) cbType.getSelectedItem());
            // Set default USU campus coordinates
            building.setLatitude(3.5651891);
            building.setLongitude(98.6566015);
            building.setDescription(tfDescription.getText().trim());
            
            if (buildingDAO.insertBuilding(building)) {
                JOptionPane.showMessageDialog(this, "Gedung berhasil ditambahkan!");
                MapRefreshUtil.refreshBuildings(); // Refresh Google Maps
                clearForm();
                loadBuildings();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambahkan gedung!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Format koordinat tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateBuilding() {
        try {
            Building building = buildingDAO.getBuildingById(selectedBuildingId);
            if (building != null) {
                building.setBuildingName(tfName.getText().trim());
                building.setBuildingType((Building.BuildingType) cbType.getSelectedItem());
                // Keep existing coordinates
                building.setLatitude(3.5651891);
                building.setLongitude(98.6566015);
                building.setDescription(tfDescription.getText().trim());
                
                if (buildingDAO.updateBuilding(building)) {
                    JOptionPane.showMessageDialog(this, "Gedung berhasil diupdate!");
                    MapRefreshUtil.refreshBuildings(); // Refresh Google Maps
                    clearForm();
                    loadBuildings();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal mengupdate gedung!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Format koordinat tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteBuilding() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Yakin ingin menghapus gedung ini?", 
            "Konfirmasi", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (buildingDAO.deleteBuilding(selectedBuildingId)) {
                JOptionPane.showMessageDialog(this, "Gedung berhasil dihapus!");
                MapRefreshUtil.refreshBuildings(); // Refresh Google Maps
                clearForm();
                loadBuildings();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus gedung!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearForm() {
        tfName.setText("");
        tfDescription.setText("");
        cbType.setSelectedIndex(0);
        selectedBuildingId = -1;
        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        buildingTable.clearSelection();
    }
}
