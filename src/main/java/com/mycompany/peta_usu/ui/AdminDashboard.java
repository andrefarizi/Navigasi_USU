package com.mycompany.peta_usu.ui;

import com.mycompany.peta_usu.PETA_USU;
import com.mycompany.peta_usu.LoginFrame;
import com.mycompany.peta_usu.dao.BuildingDAO;
import com.mycompany.peta_usu.middleware.AuthMiddleware;
import com.mycompany.peta_usu.models.Building;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Admin Dashboard - Versi Clean tanpa Search, Profile, Settings
 * Hanya Homepage, Maps, dan CRUD Panels untuk semua tabel
 */
public class AdminDashboard extends JFrame {
    private JPanel sidebarPanel, contentPanel;
    private JPanel homePanel, statsPanel;
    private JLabel lblTotalGedung, lblTotalFakultas;
    
    public AdminDashboard() {
        // Check middleware - pastikan user adalah admin
        try {
            AuthMiddleware.requireAdmin();
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(null,
                "Akses ditolak! Hanya admin yang dapat mengakses halaman ini.",
                "Access Denied",
                JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        
        initComponents();
        loadDashboardStatistics();
        setVisible(true);
    }
    
    private void initComponents() {
        setTitle("Admin Dashboard - Peta USU");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Sidebar
        createSidebar();
        add(sidebarPanel, BorderLayout.WEST);
        
        // Content Panel
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(Color.WHITE);
        
        // Home Panel dengan statistik
        createHomePanel();
        contentPanel.add(homePanel, "HOME");
        
        // CRUD Panels
        contentPanel.add(new ReportsPanel(), "REPORTS");
        contentPanel.add(new AdminMapPanel(AuthMiddleware.getCurrentUser().getUserId()), "MARKERS");
        contentPanel.add(new RoadMapPanel(AuthMiddleware.getCurrentUser().getUserId()), "ROADMAP");
        contentPanel.add(new RoadClosurePanel(AuthMiddleware.getCurrentUser().getUserId()), "CLOSURES");
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(220, 700));
        sidebarPanel.setBackground(new Color(0, 153, 153));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        
        // Logo/Title
        JLabel title = new JLabel("PetaUSU Admin");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        sidebarPanel.add(title);
        
        // Menu Items
        addMenuItem("🏠 Beranda", "HOME");
        addMenuItem("📬 Laporan User", "REPORTS");
        addMenuItem("📍 Marker Peta", "MARKERS");
        addMenuItem("🛣️ Peta Jalan", "ROADMAP");
        addMenuItem("🚧 Penutupan Jalan", "CLOSURES");
        
        sidebarPanel.add(Box.createVerticalGlue());
        
        // Logout Button
        JButton btnLogout = new JButton("🚪 Keluar");
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.setMaximumSize(new Dimension(180, 40));
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> logout());
        sidebarPanel.add(btnLogout);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));
    }
    
    private void addMenuItem(String text, String panelName) {
        JPanel menuItem = new JPanel();
        menuItem.setLayout(new BorderLayout());
        menuItem.setMaximumSize(new Dimension(220, 45));
        menuItem.setBackground(new Color(0, 153, 153));
        menuItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel label = new JLabel("  " + text);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        label.setForeground(Color.WHITE);
        menuItem.add(label, BorderLayout.CENTER);
        
        menuItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPanel(panelName);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                menuItem.setBackground(new Color(0, 180, 180));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                menuItem.setBackground(new Color(0, 153, 153));
            }
        });
        
        sidebarPanel.add(menuItem);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    }
    
    private void createHomePanel() {
        homePanel = new JPanel(new BorderLayout());
        homePanel.setBackground(Color.WHITE);
        homePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel title = new JLabel("Dashboard Admin - Navigasi USU");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        homePanel.add(title, BorderLayout.NORTH);
        
        // Statistics Panel
        statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
        
        // Stat Cards
        JPanel cardFakultas = createStatCard("Total Fakultas", "0", new Color(255, 193, 7));
        lblTotalFakultas = (JLabel) ((JPanel)cardFakultas.getComponent(1)).getComponent(0);
        
        JPanel cardGedung = createStatCard("Total Gedung", "0", new Color(0, 123, 255));
        lblTotalGedung = (JLabel) ((JPanel)cardGedung.getComponent(1)).getComponent(0);
        
        JPanel cardRuangan = createStatCard("Total Ruangan", "...", new Color(40, 167, 69));
        JPanel cardMarker = createStatCard("Total Marker", "...", new Color(108, 117, 125));
        
        statsPanel.add(cardFakultas);
        statsPanel.add(cardGedung);
        statsPanel.add(cardRuangan);
        statsPanel.add(cardMarker);
        
        homePanel.add(statsPanel, BorderLayout.CENTER);
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createLineBorder(color.darker(), 2));
        card.setPreferredSize(new Dimension(250, 120));
        
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        
        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        valuePanel.setBackground(color);
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.BOLD, 32));
        lblValue.setForeground(Color.WHITE);
        valuePanel.add(lblValue);
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valuePanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void loadDashboardStatistics() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private int totalFakultas = 0;
            private int totalGedung = 0;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    BuildingDAO buildingDAO = new BuildingDAO();
                    List<Building> allBuildings = buildingDAO.getAllBuildings();
                    
                    totalFakultas = (int) allBuildings.stream()
                        .filter(b -> b.getBuildingType() == Building.BuildingType.FAKULTAS)
                        .count();
                    
                    totalGedung = allBuildings.size();
                } catch (Exception e) {
                    System.err.println("Failed to load statistics: " + e.getMessage());
                }
                return null;
            }
            
            @Override
            protected void done() {
                lblTotalFakultas.setText(String.valueOf(totalFakultas));
                lblTotalGedung.setText(String.valueOf(totalGedung));
            }
        };
        worker.execute();
    }
    
    private void showPanel(String panelName) {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, panelName);
        
        // Refresh statistics if returning to home
        if ("HOME".equals(panelName)) {
            loadDashboardStatistics();
        }
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Yakin ingin kembali ke halaman utama?",
            "Konfirmasi Keluar",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            AuthMiddleware.logout();
            this.dispose();
            
            // Return to welcome screen (halaman hijau)
            PETA_USU.main(new String[]{});
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboard());
    }
}
