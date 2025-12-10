package com.mycompany.peta_usu.ui;

import com.mycompany.peta_usu.dao.RoomDAO;
import com.mycompany.peta_usu.dao.BuildingDAO;
import com.mycompany.peta_usu.models.Room;
import com.mycompany.peta_usu.models.Building;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Panel CRUD untuk Rooms
 * 
 * === 4 PILAR OOP ===
 * 1. ENCAPSULATION: Field roomDAO, buildingDAO, textfields PRIVATE
 * 2. INHERITANCE: Extends JPanel (parent: javax.swing.JPanel)
 *    Mewarisi method dari JPanel:
 *    • setLayout() - set layout manager
 *    • add() - tambah component
 *    • setBackground() - set background color
 *    • revalidate(), repaint() - refresh UI
 * 3. POLYMORPHISM: Override isCellEditable() + toString() di Building
 * 4. ABSTRACTION: Method loadRooms() sembunyikan JOIN query complexity
 */
public class RoomManagementPanel extends JPanel {  // ← INHERITANCE dari javax.swing.JPanel
    // ========== ENCAPSULATION: Semua field PRIVATE ==========
    private RoomDAO roomDAO;              // ← PRIVATE: Database access
    private BuildingDAO buildingDAO;      // ← PRIVATE: Database access
    private JTable roomTable;             // ← PRIVATE: UI table
    private DefaultTableModel tableModel; // ← PRIVATE: Table data model
    private JTextField tfRoomCode, tfRoomName, tfFloor, tfCapacity, tfDescription;  // ← PRIVATE
    private JComboBox<Building> cbBuilding;       // ← PRIVATE: Dropdown gedung
    private JComboBox<Room.RoomType> cbRoomType;  // ← PRIVATE: Dropdown tipe
    private JCheckBox chkAvailable;       // ← PRIVATE: Checkbox status
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnRefresh;  // ← PRIVATE
    private int selectedRoomId = -1;      // ← PRIVATE: Selected state
    
    public RoomManagementPanel() {
        roomDAO = new RoomDAO();
        buildingDAO = new BuildingDAO();
        initComponents();
        loadBuildings();
        loadRooms();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Title
        JLabel title = new JLabel("🚪 Manajemen Ruangan/Kelas");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "Gedung", "Kode", "Nama Ruangan", "Lantai", "Tipe", "Kapasitas", "Status"};
        // ========== POLYMORPHISM: Anonymous class + Override ==========
        tableModel = new DefaultTableModel(columns, 0) {
            @Override  // ← POLYMORPHISM: Override untuk custom behavior
            public boolean isCellEditable(int row, int column) {
                return false;  // Read-only table (tidak bisa diedit)
            }
        };
        roomTable = new JTable(tableModel);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomTable.setRowHeight(25);
        roomTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectRow();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(roomTable);
        scrollPane.setPreferredSize(new Dimension(900, 300));
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Ruangan"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Row 0 - Building & Code
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Gedung:"), gbc);
        gbc.gridx = 1;
        cbBuilding = new JComboBox<>();
        formPanel.add(cbBuilding, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("  Kode:"), gbc);
        gbc.gridx = 3;
        tfRoomCode = new JTextField(10);
        formPanel.add(tfRoomCode, gbc);
        
        // Row 1 - Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Nama Ruangan:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        tfRoomName = new JTextField(30);
        formPanel.add(tfRoomName, gbc);
        
        // Row 2 - Floor, Type, Capacity
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Lantai:"), gbc);
        gbc.gridx = 1;
        tfFloor = new JTextField(5);
        formPanel.add(tfFloor, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("  Tipe:"), gbc);
        gbc.gridx = 3;
        cbRoomType = new JComboBox<>(Room.RoomType.values());
        formPanel.add(cbRoomType, gbc);
        
        // Row 3 - Capacity & Available
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Kapasitas:"), gbc);
        gbc.gridx = 1;
        tfCapacity = new JTextField(10);
        formPanel.add(tfCapacity, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("  Tersedia:"), gbc);
        gbc.gridx = 3;
        chkAvailable = new JCheckBox();
        chkAvailable.setSelected(true);
        formPanel.add(chkAvailable, gbc);
        
        // Row 4 - Description
        gbc.gridx = 0; gbc.gridy = 4;
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
        btnAdd.addActionListener(e -> addRoom());
        
        btnUpdate = new JButton("✏️ Update");
        btnUpdate.setBackground(new Color(0, 123, 255));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setEnabled(false);
        btnUpdate.addActionListener(e -> updateRoom());
        
        btnDelete = new JButton("🗑️ Hapus");
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(e -> deleteRoom());
        
        btnClear = new JButton("🔄 Bersihkan");
        btnClear.addActionListener(e -> clearForm());
        
        btnRefresh = new JButton("♻️ Muat Ulang");
        btnRefresh.addActionListener(e -> loadRooms());
        
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
    
    private void loadRooms() {
        tableModel.setRowCount(0);
        List<Room> rooms = roomDAO.searchRooms(""); // Get all rooms
        for (Room r : rooms) {
            Object[] row = {
                r.getRoomId(),
                r.getBuildingName(),
                r.getRoomCode(),
                r.getRoomName(),
                r.getFloorNumber(),
                r.getRoomType().name(),
                r.getCapacity(),
                r.isAvailable() ? "Tersedia" : "Tidak Tersedia"
            };
            tableModel.addRow(row);
        }
    }
    
    private void selectRow() {
        int row = roomTable.getSelectedRow();
        if (row >= 0) {
            selectedRoomId = (int) tableModel.getValueAt(row, 0);
            Room room = roomDAO.getRoomById(selectedRoomId);
            if (room != null) {
                // Select building
                for (int i = 0; i < cbBuilding.getItemCount(); i++) {
                    if (cbBuilding.getItemAt(i).getBuildingId() == room.getBuildingId()) {
                        cbBuilding.setSelectedIndex(i);
                        break;
                    }
                }
                tfRoomCode.setText(room.getRoomCode());
                tfRoomName.setText(room.getRoomName());
                tfFloor.setText(String.valueOf(room.getFloorNumber()));
                cbRoomType.setSelectedItem(room.getRoomType());
                tfCapacity.setText(String.valueOf(room.getCapacity()));
                chkAvailable.setSelected(room.isAvailable());
                tfDescription.setText(room.getDescription());
                
                btnUpdate.setEnabled(true);
                btnDelete.setEnabled(true);
                btnAdd.setEnabled(false);
            }
        }
    }
    
    private void addRoom() {
        try {
            Building selectedBuilding = (Building) cbBuilding.getSelectedItem();
            if (selectedBuilding == null) {
                JOptionPane.showMessageDialog(this, "Pilih gedung terlebih dahulu!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Room room = new Room();
            room.setBuildingId(selectedBuilding.getBuildingId());
            room.setRoomCode(tfRoomCode.getText().trim());
            room.setRoomName(tfRoomName.getText().trim());
            room.setFloorNumber(Integer.parseInt(tfFloor.getText().trim()));
            room.setRoomType((Room.RoomType) cbRoomType.getSelectedItem());
            room.setCapacity(Integer.parseInt(tfCapacity.getText().trim()));
            room.setAvailable(chkAvailable.isSelected());
            room.setDescription(tfDescription.getText().trim());
            
            if (roomDAO.insertRoom(room)) {
                JOptionPane.showMessageDialog(this, "Ruangan berhasil ditambahkan!");
                clearForm();
                loadRooms();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambahkan ruangan!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Format angka tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateRoom() {
        try {
            Room room = roomDAO.getRoomById(selectedRoomId);
            if (room != null) {
                Building selectedBuilding = (Building) cbBuilding.getSelectedItem();
                room.setBuildingId(selectedBuilding.getBuildingId());
                room.setRoomCode(tfRoomCode.getText().trim());
                room.setRoomName(tfRoomName.getText().trim());
                room.setFloorNumber(Integer.parseInt(tfFloor.getText().trim()));
                room.setRoomType((Room.RoomType) cbRoomType.getSelectedItem());
                room.setCapacity(Integer.parseInt(tfCapacity.getText().trim()));
                room.setAvailable(chkAvailable.isSelected());
                room.setDescription(tfDescription.getText().trim());
                
                if (roomDAO.updateRoom(room)) {
                    JOptionPane.showMessageDialog(this, "Ruangan berhasil diupdate!");
                    clearForm();
                    loadRooms();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal mengupdate ruangan!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Format angka tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteRoom() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Yakin ingin menghapus ruangan ini?", 
            "Konfirmasi", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (roomDAO.deleteRoom(selectedRoomId)) {
                JOptionPane.showMessageDialog(this, "Ruangan berhasil dihapus!");
                clearForm();
                loadRooms();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus ruangan!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearForm() {
        tfRoomCode.setText("");
        tfRoomName.setText("");
        tfFloor.setText("");
        tfCapacity.setText("");
        tfDescription.setText("");
        cbRoomType.setSelectedIndex(0);
        chkAvailable.setSelected(true);
        selectedRoomId = -1;
        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        roomTable.clearSelection();
    }
}
