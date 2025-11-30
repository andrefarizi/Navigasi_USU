package com.mycompany.peta_usu.ui;

import com.mycompany.peta_usu.dao.RoadDAO;
import com.mycompany.peta_usu.models.Road;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Panel CRUD untuk Roads
 */
public class RoadManagementPanel extends JPanel {
    private RoadDAO roadDAO;
    private JTable roadTable;
    private DefaultTableModel tableModel;
    private JTextField tfName, tfStartLat, tfStartLng, tfEndLat, tfEndLng, tfDistance, tfDescription;
    private JComboBox<Road.RoadType> cbType;
    private JCheckBox chkOneWay;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnRefresh;
    private int selectedRoadId = -1;
    
    public RoadManagementPanel() {
        roadDAO = new RoadDAO();
        initComponents();
        loadRoads();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Title
        JLabel title = new JLabel("🛣️ Manajemen Jalan Kampus");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "Nama Jalan", "Tipe", "Satu Arah", "Jarak (m)", "Deskripsi"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        roadTable = new JTable(tableModel);
        roadTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roadTable.setRowHeight(25);
        roadTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectRow();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(roadTable);
        scrollPane.setPreferredSize(new Dimension(900, 300));
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Jalan"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Row 0 - Name & Type
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nama Jalan:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        tfName = new JTextField(20);
        formPanel.add(tfName, gbc);
        gbc.gridx = 3; gbc.gridwidth = 1;
        formPanel.add(new JLabel("  Tipe:"), gbc);
        gbc.gridx = 4;
        cbType = new JComboBox<>(Road.RoadType.values());
        formPanel.add(cbType, gbc);
        
        // Hidden coordinate fields (will use default values)
        tfStartLat = new JTextField(12);
        tfStartLat.setVisible(false);
        tfStartLng = new JTextField(12);
        tfStartLng.setVisible(false);
        tfEndLat = new JTextField(12);
        tfEndLat.setVisible(false);
        tfEndLng = new JTextField(12);
        tfEndLng.setVisible(false);
        
        // Row 1 - Distance & One Way
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Jarak (m):"), gbc);
        gbc.gridx = 1;
        tfDistance = new JTextField(10);
        formPanel.add(tfDistance, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("  Satu Arah:"), gbc);
        gbc.gridx = 3;
        chkOneWay = new JCheckBox();
        formPanel.add(chkOneWay, gbc);
        
        // Row 4 - Description
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Deskripsi:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 4;
        tfDescription = new JTextField(30);
        formPanel.add(tfDescription, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        btnAdd = new JButton("➕ Tambah");
        btnAdd.setBackground(new Color(40, 167, 69));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> addRoad());
        
        btnUpdate = new JButton("✏️ Update");
        btnUpdate.setBackground(new Color(0, 123, 255));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setEnabled(false);
        btnUpdate.addActionListener(e -> updateRoad());
        
        btnDelete = new JButton("🗑️ Hapus");
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(e -> deleteRoad());
        
        btnClear = new JButton("🔄 Bersihkan");
        btnClear.addActionListener(e -> clearForm());
        
        btnRefresh = new JButton("♻️ Muat Ulang");
        btnRefresh.addActionListener(e -> loadRoads());
        
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
    
    private void loadRoads() {
        tableModel.setRowCount(0);
        List<Road> roads = roadDAO.getAllRoads();
        for (Road r : roads) {
            Object[] row = {
                r.getRoadId(),
                r.getRoadName(),
                r.getRoadType().getDisplayName(),
                r.isOneWay() ? "Ya" : "Tidak",
                String.format("%.2f", r.getDistance()),
                r.getDescription() != null ? r.getDescription() : "-"
            };
            tableModel.addRow(row);
        }
    }
    
    private void selectRow() {
        int row = roadTable.getSelectedRow();
        if (row >= 0) {
            selectedRoadId = (int) tableModel.getValueAt(row, 0);
            Road road = roadDAO.getRoadById(selectedRoadId);
            if (road != null) {
                tfName.setText(road.getRoadName());
                cbType.setSelectedItem(road.getRoadType());
                // Set default coordinates (hidden)
                tfStartLat.setText("3.5651891");
                tfStartLng.setText("98.6566015");
                tfEndLat.setText("3.5651891");
                tfEndLng.setText("98.6566015");
                tfDistance.setText(String.format("%.2f", road.getDistance()));
                chkOneWay.setSelected(road.isOneWay());
                tfDescription.setText(road.getDescription());
                
                btnUpdate.setEnabled(true);
                btnDelete.setEnabled(true);
                btnAdd.setEnabled(false);
            }
        }
    }
    
    private void addRoad() {
        try {
            Road road = new Road();
            road.setRoadName(tfName.getText().trim());
            road.setRoadType((Road.RoadType) cbType.getSelectedItem());
            // Set default USU coordinates
            road.setStartLat(3.5651891);
            road.setStartLng(98.6566015);
            road.setEndLat(3.5651891);
            road.setEndLng(98.6566015);
            road.setDistance(Double.parseDouble(tfDistance.getText().trim()));
            road.setOneWay(chkOneWay.isSelected());
            road.setDescription(tfDescription.getText().trim());
            
            if (roadDAO.insertRoad(road)) {
                JOptionPane.showMessageDialog(this, "Jalan berhasil ditambahkan!");
                clearForm();
                loadRoads();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambahkan jalan!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Format angka tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateRoad() {
        try {
            Road road = roadDAO.getRoadById(selectedRoadId);
            if (road != null) {
                road.setRoadName(tfName.getText().trim());
                road.setRoadType((Road.RoadType) cbType.getSelectedItem());
                // Keep default coordinates
                road.setStartLat(3.5651891);
                road.setStartLng(98.6566015);
                road.setEndLat(3.5651891);
                road.setEndLng(98.6566015);
                road.setDistance(Double.parseDouble(tfDistance.getText().trim()));
                road.setOneWay(chkOneWay.isSelected());
                road.setDescription(tfDescription.getText().trim());
                
                if (roadDAO.updateRoad(road)) {
                    JOptionPane.showMessageDialog(this, "Jalan berhasil diupdate!");
                    clearForm();
                    loadRoads();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal mengupdate jalan!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Format angka tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteRoad() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Yakin ingin menghapus jalan ini?", 
            "Konfirmasi", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (roadDAO.deleteRoad(selectedRoadId)) {
                JOptionPane.showMessageDialog(this, "Jalan berhasil dihapus!");
                clearForm();
                loadRoads();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus jalan!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearForm() {
        tfName.setText("");
        tfStartLat.setText("");
        tfStartLng.setText("");
        tfEndLat.setText("");
        tfEndLng.setText("");
        tfDistance.setText("");
        tfDescription.setText("");
        cbType.setSelectedIndex(0);
        chkOneWay.setSelected(false);
        selectedRoadId = -1;
        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        roadTable.clearSelection();
    }
}
