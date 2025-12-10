package com.mycompany.peta_usu.ui;

import javax.swing.*;
import java.awt.*;

/**
 * AdminMainFrame - Main window untuk admin management
 * Menggabungkan AdminMapPanel dan RoadClosurePanel
 * 
 * === 4 PILAR OOP ===
 * 1. ENCAPSULATION: Field adminUserId, tabbedPane PRIVATE
 * 2. INHERITANCE: Extends JFrame (parent: javax.swing.JFrame)
 *    Mewarisi method dari JFrame:
 *    • setTitle() - set window title
 *    • setSize() - set window size
 *    • setDefaultCloseOperation() - set close behavior
 *    • setLocationRelativeTo() - center window
 *    • setVisible() - show/hide window
 *    • add() - add component
 *    • dispose() - close window
 * 3. POLYMORPHISM: Override createIcon() untuk multi-purpose icon
 * 4. ABSTRACTION: Method createHeaderPanel() sembunyikan UI creation
 * 
 * @author PETA_USU Team
 */
public class AdminMainFrame extends JFrame {  // ← INHERITANCE dari javax.swing.JFrame
    
    // ========== ENCAPSULATION: Field PRIVATE ==========
    private int adminUserId;        // ← PRIVATE: Session admin user
    private JTabbedPane tabbedPane; // ← PRIVATE: Tab container
    
    public AdminMainFrame(int userId) {
        this.adminUserId = userId;
        
        setTitle("PetaUSU - Admin Panel");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        
        initComponents();
    }
    
    private void initComponents() {
        // Main layout
        setLayout(new BorderLayout());
        
        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Tabbed pane untuk berbagai management panels
        tabbedPane = new JTabbedPane();
        
        // Add tabs - Laporan User di posisi pertama
        tabbedPane.addTab("Laporan User", createIcon("📬"), 
            new ReportsPanel(), 
            "Lihat dan kelola laporan pengguna");
        
        tabbedPane.addTab("Dashboard", createIcon("📊"), 
            createDashboardPanel(), 
            "Lihat statistik dan ringkasan");
        
        tabbedPane.addTab("Penanda Peta", createIcon("📍"), 
            new AdminMapPanel(adminUserId), 
            "Kelola penanda gedung dan ikon");
        
        tabbedPane.addTab("Penutupan Jalan", createIcon("🚧"), 
            new RoadClosurePanel(adminUserId), 
            "Kelola penutupan jalan dan jalan satu arah");
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Footer
        add(createFooterPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(56, 136, 96));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Sistem Manajemen Admin PetaUSU");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Kelola penanda, gedung, dan informasi jalan");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(220, 255, 220));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        // Logout button
        JButton btnLogout = new JButton("Keluar");
        btnLogout.setBackground(new Color(244, 67, 54));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Apakah Anda yakin ingin keluar?", 
                "Konfirmasi Keluar", 
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                // Buka login frame
                SwingUtilities.invokeLater(() -> {
                    // new LoginFrame().setVisible(true);
                    JOptionPane.showMessageDialog(null, "Berhasil keluar!");
                });
            }
        });
        
        panel.add(btnLogout, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel lblFooter = new JLabel("© 2025 PetaUSU - Universitas Sumatera Utara Navigation System");
        lblFooter.setFont(new Font("Arial", Font.PLAIN, 11));
        lblFooter.setForeground(Color.GRAY);
        
        panel.add(lblFooter);
        
        return panel;
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        
        // Statistics cards
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(createStatCard("Total Buildings", "8", new Color(33, 150, 243)), gbc);
        
        gbc.gridx = 1;
        panel.add(createStatCard("Total Markers", "15", new Color(76, 175, 80)), gbc);
        
        gbc.gridx = 2;
        panel.add(createStatCard("Active Closures", "3", new Color(255, 152, 0)), gbc);
        
        // Recent activity
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weighty = 2.0;
        panel.add(createRecentActivityPanel(), gbc);
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.BOLD, 36));
        lblValue.setForeground(Color.WHITE);
        lblValue.setHorizontalAlignment(JLabel.CENTER);
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createRecentActivityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Recent Activity"));
        
        JTextArea txtActivity = new JTextArea();
        txtActivity.setEditable(false);
        txtActivity.setText(
            "Recent changes:\n\n" +
            "• Added marker: Fakultas Kedokteran (FK)\n" +
            "• Updated: Road closure on Jalan Dr. Mansyur\n" +
            "• Added marker: Stadium USU\n" +
            "• Removed: Temporary closure on Jalan Perpustakaan\n\n" +
            "Note: Activity log akan di-load dari database"
        );
        
        panel.add(new JScrollPane(txtActivity), BorderLayout.CENTER);
        
        return panel;
    }
    
    private Icon createIcon(String emoji) {
        JLabel label = new JLabel(emoji);
        label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        return new ImageIcon(createImageFromLabel(label));
    }
    
    private Image createImageFromLabel(JLabel label) {
        int width = 16;
        int height = 16;
        
        label.setSize(width, height);
        
        Image img = new java.awt.image.BufferedImage(
            width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics g = img.getGraphics();
        label.paint(g);
        g.dispose();
        
        return img;
    }
    
    /**
     * Launch admin panel
     */
    public static void launch(int adminUserId) {
        SwingUtilities.invokeLater(() -> {
            AdminMainFrame frame = new AdminMainFrame(adminUserId);
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
        
        // Launch dengan user ID 1 (admin)
        launch(1);
    }
}
