package com.mycompany.peta_usu.ui;

import com.mycompany.peta_usu.dao.FacilityDAO;
import com.mycompany.peta_usu.dao.BuildingDAO;
import com.mycompany.peta_usu.models.Facility;
import com.mycompany.peta_usu.models.Building;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Panel CRUD untuk Facilities
 */
public class FacilityManagementPanel extends JPanel {
    private FacilityDAO facilityDAO;
    private BuildingDAO buildingDAO;
    private JTable facilityTable;
    private DefaultTableModel tableModel;
    private JTextField tfName, tfDescription;
    private JComboBox<Building> cbBuilding;
    private JComboBox<Facility.FacilityType> cbType;
    private JCheckBox chkAvailable;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnRefresh;
    private int selectedFacilityId = -1;
    
    public FacilityManagementPanel() {
        facilityDAO = new FacilityDAO();
        buildingDAO = new BuildingDAO();
        initComponents();
        loadBuildings();
        loadFacilities();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Title
        JLabel title = new JLabel("🏪 Manajemen Fasilitas Kampus");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "Gedung", "Nama Fasilitas", "Tipe", "Deskripsi", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        facilityTable = new JTable(tableModel);
        facilityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        facilityTable.setRowHeight(25);
        facilityTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectRow();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(facilityTable);
        scrollPane.setPreferredSize(new Dimension(900, 300));
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Fasilitas"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Row 0 - Building & Type
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Gedung:"), gbc);
        gbc.gridx = 1;
        cbBuilding = new JComboBox<>();
        formPanel.add(cbBuilding, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("  Tipe:"), gbc);
        gbc.gridx = 3;
        cbType = new JComboBox<>(Facility.FacilityType.values());
        formPanel.add(cbType, gbc);
        
        // Row 1 - Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Nama Fasilitas:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        tfName = new JTextField(30);
        formPanel.add(tfName, gbc);
        
        // Row 2 - Description & Available
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Deskripsi:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        tfDescription = new JTextField(25);
        formPanel.add(tfDescription, gbc);
        gbc.gridx = 3; gbc.gridwidth = 1;
        chkAvailable = new JCheckBox("Tersedia");
        chkAvailable.setSelected(true);
        formPanel.add(chkAvailable, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        btnAdd = new JButton("➕ Tambah");
        btnAdd.setBackground(new Color(40, 167, 69));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> addFacility());
        
        btnUpdate = new JButton("✏️ Update");
        btnUpdate.setBackground(new Color(0, 123, 255));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setEnabled(false);
        btnUpdate.addActionListener(e -> updateFacility());
        
        btnDelete = new JButton("🗑️ Hapus");
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(e -> deleteFacility());
        
        btnClear = new JButton("🔄 Bersihkan");
        btnClear.addActionListener(e -> clearForm());
        
        btnRefresh = new JButton("♻️ Muat Ulang");
        btnRefresh.addActionListener(e -> loadFacilities());
        
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
        cbBuilding.removeAllItems();
        List<Building> buildings = buildingDAO.getAllBuildings();
        for (Building b : buildings) {
            cbBuilding.addItem(b);
        }
    }
    
    private void loadFacilities() {
        tableModel.setRowCount(0);
        List<Facility> facilities = facilityDAO.getAllFacilities();
        for (Facility f : facilities) {
            Object[] row = {
                f.getFacilityId(),
                f.getBuildingName(),
                f.getFacilityName(),
                f.getFacilityType().getDisplayName(),
                f.getDescription(),
                f.isAvailable() ? "Tersedia" : "Tidak Tersedia"
            };
            tableModel.addRow(row);
        }
    }
    
    private void selectRow() {
        int row = facilityTable.getSelectedRow();
        if (row >= 0) {
            selectedFacilityId = (int) tableModel.getValueAt(row, 0);
            Facility facility = facilityDAO.getFacilityById(selectedFacilityId);
            if (facility != null) {
                // Select building
                for (int i = 0; i < cbBuilding.getItemCount(); i++) {
                    if (cbBuilding.getItemAt(i).getBuildingId() == facility.getBuildingId()) {
                        cbBuilding.setSelectedIndex(i);
                        break;
                    }
                }
                tfName.setText(facility.getFacilityName());
                cbType.setSelectedItem(facility.getFacilityType());
                tfDescription.setText(facility.getDescription());
                chkAvailable.setSelected(facility.isAvailable());
                
                btnUpdate.setEnabled(true);
                btnDelete.setEnabled(true);
                btnAdd.setEnabled(false);
            }
        }
    }
    
    private void addFacility() {
        Building selectedBuilding = (Building) cbBuilding.getSelectedItem();
        if (selectedBuilding == null) {
            JOptionPane.showMessageDialog(this, "Pilih gedung terlebih dahulu!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Facility facility = new Facility();
        facility.setBuildingId(selectedBuilding.getBuildingId());
        facility.setFacilityName(tfName.getText().trim());
        facility.setFacilityType((Facility.FacilityType) cbType.getSelectedItem());
        facility.setDescription(tfDescription.getText().trim());
        facility.setAvailable(chkAvailable.isSelected());
        
        if (facilityDAO.insertFacility(facility)) {
            JOptionPane.showMessageDialog(this, "Fasilitas berhasil ditambahkan!");
            clearForm();
            loadFacilities();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menambahkan fasilitas!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateFacility() {
        Facility facility = facilityDAO.getFacilityById(selectedFacilityId);
        if (facility != null) {
            Building selectedBuilding = (Building) cbBuilding.getSelectedItem();
            facility.setBuildingId(selectedBuilding.getBuildingId());
            facility.setFacilityName(tfName.getText().trim());
            facility.setFacilityType((Facility.FacilityType) cbType.getSelectedItem());
            facility.setDescription(tfDescription.getText().trim());
            facility.setAvailable(chkAvailable.isSelected());
            
            if (facilityDAO.updateFacility(facility)) {
                JOptionPane.showMessageDialog(this, "Fasilitas berhasil diupdate!");
                clearForm();
                loadFacilities();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mengupdate fasilitas!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteFacility() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Yakin ingin menghapus fasilitas ini?", 
            "Konfirmasi", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (facilityDAO.deleteFacility(selectedFacilityId)) {
                JOptionPane.showMessageDialog(this, "Fasilitas berhasil dihapus!");
                clearForm();
                loadFacilities();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus fasilitas!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearForm() {
        tfName.setText("");
        tfDescription.setText("");
        cbType.setSelectedIndex(0);
        chkAvailable.setSelected(true);
        selectedFacilityId = -1;
        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        facilityTable.clearSelection();
    }
}
