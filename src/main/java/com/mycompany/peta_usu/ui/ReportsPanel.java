package com.mycompany.peta_usu.ui;

import com.mycompany.peta_usu.dao.ReportDAO;
import com.mycompany.peta_usu.models.Report;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Panel for admin to view and manage user reports
 * 
 * === 4 PILAR OOP ===
 * 1. ENCAPSULATION: Field reportDAO, table, lblUnreadCount PRIVATE
 * 2. INHERITANCE: Extends JPanel (parent: javax.swing.JPanel)
 *    Mewarisi method dari JPanel:
 *    • setLayout() - set layout manager
 *    • add() - tambah component
 *    • setBackground() - set background color
 *    • setBorder() - set border
 *    • revalidate() - refresh layout
 *    • repaint() - redraw component
 * 3. POLYMORPHISM: Override isCellEditable() untuk read-only table
 * 4. ABSTRACTION: Method loadReports() sembunyikan DAO.getAllReports()
 */
public class ReportsPanel extends JPanel {  // ← INHERITANCE dari javax.swing.JPanel
    
    // ========== ENCAPSULATION: Constants PRIVATE STATIC FINAL ==========
    private static final Color PRIMARY_GREEN = new Color(46, 125, 50);  // ← PRIVATE
    private static final Font TITLE_FONT = new Font("Times New Roman", Font.BOLD, 20);  // ← PRIVATE
    private static final Font MAIN_FONT = new Font("Times New Roman", Font.PLAIN, 13);  // ← PRIVATE
    
    // ========== ENCAPSULATION: Field PRIVATE ==========
    private JTable reportsTable;          // ← PRIVATE: UI component
    private DefaultTableModel tableModel; // ← PRIVATE: Data model
    private ReportDAO reportDAO;          // ← PRIVATE: Database access
    private JLabel lblUnreadCount;        // ← PRIVATE: UI label
    
    public ReportsPanel() {
        reportDAO = new ReportDAO();
        initComponents();
        loadReports();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("📬 Laporan User");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_GREEN);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Unread count badge
        lblUnreadCount = new JLabel();
        lblUnreadCount.setFont(new Font("Times New Roman", Font.BOLD, 14));
        lblUnreadCount.setForeground(Color.WHITE);
        lblUnreadCount.setBackground(new Color(220, 20, 60));
        lblUnreadCount.setOpaque(true);
        lblUnreadCount.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        headerPanel.add(lblUnreadCount, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "NIM", "Nama", "Lokasi", "Tipe", "Deskripsi", "Waktu", "Status"};
        // ========== POLYMORPHISM: Anonymous inner class + Override ==========
        tableModel = new DefaultTableModel(columns, 0) {
            @Override  // ← POLYMORPHISM: Override untuk custom behavior
            public boolean isCellEditable(int row, int column) {
                return false;  // All cells read-only (tidak bisa diedit)
            }
        };
        
        reportsTable = new JTable(tableModel);
        reportsTable.setFont(MAIN_FONT);
        reportsTable.setRowHeight(30);
        reportsTable.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 13));
        reportsTable.getTableHeader().setBackground(PRIMARY_GREEN);
        reportsTable.getTableHeader().setForeground(Color.WHITE);
        reportsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Column widths
        reportsTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        reportsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // NIM
        reportsTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Nama
        reportsTable.getColumnModel().getColumn(3).setPreferredWidth(200); // Lokasi
        reportsTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Tipe
        reportsTable.getColumnModel().getColumn(5).setPreferredWidth(300); // Deskripsi
        reportsTable.getColumnModel().getColumn(6).setPreferredWidth(150); // Waktu
        reportsTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Status
        
        // Center align for certain columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        reportsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        reportsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        reportsTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        reportsTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
        
        JScrollPane scrollPane = new JScrollPane(reportsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(PRIMARY_GREEN, 1));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setFont(MAIN_FONT);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadReports());
        buttonPanel.add(btnRefresh);
        
        JButton btnMarkRead = new JButton("✓ Tandai Dibaca");
        btnMarkRead.setFont(MAIN_FONT);
        btnMarkRead.setBackground(new Color(76, 175, 80));
        btnMarkRead.setForeground(Color.WHITE);
        btnMarkRead.setFocusPainted(false);
        btnMarkRead.addActionListener(e -> markAsRead());
        buttonPanel.add(btnMarkRead);
        
        JButton btnDelete = new JButton("🗑 Hapus");
        btnDelete.setFont(MAIN_FONT);
        btnDelete.setBackground(new Color(220, 20, 60));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.addActionListener(e -> deleteReport());
        buttonPanel.add(btnDelete);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    public void loadReports() {
        tableModel.setRowCount(0);
        List<Report> reports = reportDAO.getAllReports();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        for (Report report : reports) {
            Object[] row = {
                report.getReportId(),
                report.getUserNim(),
                report.getUserName(),
                report.getLocation(),
                report.getReportType().toString(),
                report.getDescription(),
                sdf.format(report.getCreatedAt()),
                report.isRead() ? "✓ Dibaca" : "● Baru"
            };
            tableModel.addRow(row);
        }
        
        // Update unread count
        int unreadCount = reportDAO.getUnreadCount();
        if (unreadCount > 0) {
            lblUnreadCount.setText("  " + unreadCount + " Baru  ");
            lblUnreadCount.setVisible(true);
        } else {
            lblUnreadCount.setVisible(false);
        }
    }
    
    private void markAsRead() {
        int selectedRow = reportsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Pilih laporan yang ingin ditandai sebagai dibaca",
                "Informasi",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int reportId = (int) tableModel.getValueAt(selectedRow, 0);
        boolean success = reportDAO.markAsRead(reportId);
        
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Laporan telah ditandai sebagai dibaca",
                "Sukses",
                JOptionPane.INFORMATION_MESSAGE);
            loadReports();
        } else {
            JOptionPane.showMessageDialog(this,
                "Gagal menandai laporan",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteReport() {
        int selectedRow = reportsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Pilih laporan yang ingin dihapus",
                "Informasi",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Yakin ingin menghapus laporan ini?",
            "Konfirmasi",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            int reportId = (int) tableModel.getValueAt(selectedRow, 0);
            boolean success = reportDAO.deleteReport(reportId);
            
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Laporan berhasil dihapus",
                    "Sukses",
                    JOptionPane.INFORMATION_MESSAGE);
                loadReports();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Gagal menghapus laporan",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
