package com.mycompany.peta_usu.ui;

import com.mycompany.peta_usu.PETA_USU;
import com.mycompany.peta_usu.LoginFrame;
import com.mycompany.peta_usu.dao.MarkerDAO;
import com.mycompany.peta_usu.dao.ReportDAO;
import com.mycompany.peta_usu.dao.RoadDAO;
import com.mycompany.peta_usu.dao.RoadClosureDAO;
import com.mycompany.peta_usu.middleware.AuthMiddleware;
import com.mycompany.peta_usu.models.Marker;
import com.mycompany.peta_usu.models.Report;
import com.mycompany.peta_usu.models.Road;
import com.mycompany.peta_usu.models.RoadClosure;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Admin Dashboard - Versi Clean tanpa Search, Profile, Settings
 * Hanya Homepage, Maps, dan CRUD Panels untuk semua tabel
 */
public class AdminDashboard extends JFrame {
    private JPanel sidebarPanel, contentPanel;
    private JPanel homePanel, statsPanel;
    private JLabel lblTotalGedung, lblTotalFakultas, lblTotalReport, lblTotalMarker;
    private JLabel lblUnreadReports, lblActiveClosures;
    private JLabel lblTotalBuilding, lblTotalFasilitas;
    private JPanel recentActivityPanel;
    private JPanel chartPanel;
    
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
        homePanel = new JPanel(new BorderLayout(10, 10));
        homePanel.setBackground(Color.WHITE);
        homePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header Panel dengan Welcome Message
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel title = new JLabel("Dashboard Admin - Navigasi USU");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        
        JLabel welcomeMsg = new JLabel("Selamat datang, " + AuthMiddleware.getCurrentUser().getName() + " 👋");
        welcomeMsg.setFont(new Font("Arial", Font.PLAIN, 14));
        welcomeMsg.setForeground(new Color(100, 100, 100));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(welcomeMsg, BorderLayout.SOUTH);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        homePanel.add(headerPanel, BorderLayout.NORTH);
        
        // Main content with scroll
        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(Color.WHITE);
        
        // Top Section - Statistics Cards
        JPanel topSection = new JPanel(new BorderLayout(10, 10));
        topSection.setBackground(Color.WHITE);
        
        // Primary Statistics Panel (2x3 Grid)
        statsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        statsPanel.setBackground(Color.WHITE);
        
        // Stat Cards dengan icon dan detail
        JPanel cardFakultas = createEnhancedStatCard("Total Fakultas", "0", "🏛️", new Color(255, 193, 7), "");
        // Component 1 = rightPanel, lalu component 2 = lblValue (setelah lblTitle dan Glue)
        lblTotalFakultas = (JLabel) ((JPanel)cardFakultas.getComponent(1)).getComponent(2);
        
        JPanel cardGedung = createEnhancedStatCard("Total Gedung", "0", "🏢", new Color(0, 123, 255), "");
        lblTotalGedung = (JLabel) ((JPanel)cardGedung.getComponent(1)).getComponent(2);
        
        JPanel cardReport = createEnhancedStatCard("Total Laporan", "0", "📬", new Color(40, 167, 69), "");
        lblTotalReport = (JLabel) ((JPanel)cardReport.getComponent(1)).getComponent(2);
        
        JPanel cardMarker = createEnhancedStatCard("Total Jalan", "0", "🛣️", new Color(108, 117, 125), "");
        lblTotalMarker = (JLabel) ((JPanel)cardMarker.getComponent(1)).getComponent(2);
        
        JPanel cardBuilding = createEnhancedStatCard("Total Building", "0", "🏗️", new Color(156, 39, 176), "");
        lblTotalBuilding = (JLabel) ((JPanel)cardBuilding.getComponent(1)).getComponent(2);
        
        JPanel cardFasilitas = createEnhancedStatCard("Total Fasilitas", "0", "🏪", new Color(0, 150, 136), "");
        lblTotalFasilitas = (JLabel) ((JPanel)cardFasilitas.getComponent(1)).getComponent(2);
        
        statsPanel.add(cardFakultas);
        statsPanel.add(cardGedung);
        statsPanel.add(cardReport);
        statsPanel.add(cardMarker);
        statsPanel.add(cardBuilding);
        statsPanel.add(cardFasilitas);
        
        topSection.add(statsPanel, BorderLayout.CENTER);
        
        // Additional Info Cards (Unread Reports & Active Closures)
        JPanel additionalStatsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        additionalStatsPanel.setBackground(Color.WHITE);
        additionalStatsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JPanel unreadCard = createInfoCard("Laporan Belum Dibaca", "0", new Color(220, 53, 69), "⚠️");
        // Component 1 = rightPanel, lalu component 2 = lblValue (setelah lblTitle dan Glue)
        lblUnreadReports = (JLabel) ((JPanel)unreadCard.getComponent(1)).getComponent(2);
        
        JPanel closuresCard = createInfoCard("Jalan Ditutup Aktif", "0", new Color(255, 152, 0), "🚧");
        lblActiveClosures = (JLabel) ((JPanel)closuresCard.getComponent(1)).getComponent(2);
        
        additionalStatsPanel.add(unreadCard);
        additionalStatsPanel.add(closuresCard);
        
        topSection.add(additionalStatsPanel, BorderLayout.SOUTH);
        mainContent.add(topSection, BorderLayout.NORTH);
        
        // Bottom Section - Recent Activity & Chart
        JPanel bottomSection = new JPanel(new GridLayout(1, 2, 15, 0));
        bottomSection.setBackground(Color.WHITE);
        bottomSection.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // Recent Activity Panel
        recentActivityPanel = createRecentActivityPanel();
        bottomSection.add(recentActivityPanel);
        
        // Chart Panel (Laporan per Bulan)
        chartPanel = createChartPanel();
        bottomSection.add(chartPanel);
        
        mainContent.add(bottomSection, BorderLayout.CENTER);
        
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        homePanel.add(scrollPane, BorderLayout.CENTER);
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
    
    private JPanel createEnhancedStatCard(String title, String value, String icon, Color color, String detail) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(color.darker(), 2, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setPreferredSize(new Dimension(280, 130));
        
        // Icon Label
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        lblIcon.setForeground(Color.WHITE);
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setVerticalAlignment(SwingConstants.CENTER);
        card.add(lblIcon, BorderLayout.WEST);
        
        // Right Panel (Title + Value)
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(color);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Value Label - This is what we'll update
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.BOLD, 48));
        lblValue.setForeground(Color.WHITE);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblValue.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        rightPanel.add(lblTitle);
        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(lblValue);
        
        card.add(rightPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createInfoCard(String title, String value, Color color, String icon) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(color.darker(), 2, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setPreferredSize(new Dimension(280, 130));
        
        // Icon Label
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        lblIcon.setForeground(Color.WHITE);
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setVerticalAlignment(SwingConstants.CENTER);
        card.add(lblIcon, BorderLayout.WEST);
        
        // Right Panel (Title + Value)
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(color);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Value Label - This is what we'll update
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.BOLD, 48));
        lblValue.setForeground(Color.WHITE);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblValue.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        rightPanel.add(lblTitle);
        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(lblValue);
        
        card.add(rightPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createRecentActivityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel title = new JLabel("📋 Aktivitas Terakhir");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);
        
        // Activity list
        JPanel activityList = new JPanel();
        activityList.setLayout(new BoxLayout(activityList, BoxLayout.Y_AXIS));
        activityList.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(activityList);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(0, 250));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createChartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel title = new JLabel("📊 Laporan Per Bulan (6 Bulan Terakhir)");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);
        
        // Chart area
        JPanel chartArea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBarChart(g, getWidth(), getHeight());
            }
        };
        chartArea.setBackground(Color.WHITE);
        chartArea.setPreferredSize(new Dimension(0, 250));
        
        panel.add(chartArea, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void drawBarChart(Graphics g, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Get report data per month
        Map<String, Integer> monthlyData = getMonthlyReportData();
        
        if (monthlyData.isEmpty()) {
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
            String msg = "Tidak ada data laporan";
            FontMetrics fm = g2d.getFontMetrics();
            int msgWidth = fm.stringWidth(msg);
            g2d.drawString(msg, (width - msgWidth) / 2, height / 2);
            return;
        }
        
        int padding = 40;
        int barWidth = (width - 2 * padding) / monthlyData.size() - 10;
        int maxHeight = height - 2 * padding;
        
        // Find max value
        int maxValue = monthlyData.values().stream().max(Integer::compareTo).orElse(1);
        if (maxValue == 0) maxValue = 1;
        
        int x = padding;
        int index = 0;
        
        for (Map.Entry<String, Integer> entry : monthlyData.entrySet()) {
            int barHeight = (int) ((double) entry.getValue() / maxValue * maxHeight);
            int y = height - padding - barHeight;
            
            // Draw bar
            g2d.setColor(new Color(40, 167, 69));
            g2d.fillRect(x, y, barWidth, barHeight);
            
            // Draw value on top
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            String valueStr = String.valueOf(entry.getValue());
            FontMetrics fm = g2d.getFontMetrics();
            int valueWidth = fm.stringWidth(valueStr);
            g2d.drawString(valueStr, x + (barWidth - valueWidth) / 2, y - 5);
            
            // Draw month label
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            String monthLabel = entry.getKey();
            int labelWidth = g2d.getFontMetrics().stringWidth(monthLabel);
            g2d.drawString(monthLabel, x + (barWidth - labelWidth) / 2, height - padding + 20);
            
            x += barWidth + 10;
            index++;
        }
    }
    
    private Map<String, Integer> getMonthlyReportData() {
        Map<String, Integer> monthlyData = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM");
        
        // Get last 6 months
        Calendar cal = Calendar.getInstance();
        for (int i = 5; i >= 0; i--) {
            cal.add(Calendar.MONTH, i == 5 ? 0 : -1);
            String monthName = sdf.format(cal.getTime());
            monthlyData.put(monthName, 0);
        }
        
        // Count reports per month
        try {
            ReportDAO reportDAO = new ReportDAO();
            List<Report> reports = reportDAO.getAllReports();
            
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
            Calendar currentCal = Calendar.getInstance();
            
            for (Report report : reports) {
                Calendar reportCal = Calendar.getInstance();
                reportCal.setTime(report.getCreatedAt());
                
                // Check if within last 6 months
                long monthsDiff = (currentCal.getTimeInMillis() - reportCal.getTimeInMillis()) / (1000L * 60 * 60 * 24 * 30);
                if (monthsDiff <= 6) {
                    String month = monthFormat.format(report.getCreatedAt());
                    monthlyData.put(month, monthlyData.getOrDefault(month, 0) + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return monthlyData;
    }
    
    private void loadRecentActivities() {
        JPanel activityList = (JPanel) ((JScrollPane) recentActivityPanel.getComponent(1)).getViewport().getView();
        activityList.removeAll();
        
        try {
            ReportDAO reportDAO = new ReportDAO();
            RoadClosureDAO closureDAO = new RoadClosureDAO();
            MarkerDAO markerDAO = new MarkerDAO();
            
            List<Report> recentReports = reportDAO.getAllReports();
            List<RoadClosure> recentClosures = closureDAO.getActiveClosures();
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            int count = 0;
            
            // Show last 5 reports
            for (int i = 0; i < Math.min(3, recentReports.size()); i++) {
                Report report = recentReports.get(i);
                String timeStr = getTimeAgo(report.getCreatedAt());
                String activity = "📬 " + report.getUserName() + " melaporkan " + 
                                report.getReportType().toString().toLowerCase().replace('_', ' ') + 
                                " - " + timeStr;
                activityList.add(createActivityItem(activity, !report.isRead()));
                count++;
            }
            
            // Show last 2 closures
            for (int i = 0; i < Math.min(2, recentClosures.size()); i++) {
                RoadClosure closure = recentClosures.get(i);
                String timeStr = getTimeAgo(closure.getCreatedAt());
                String activity = "🚧 Jalan " + closure.getRoadName() + " ditutup " + 
                                closure.getClosureType().getValue() + " - " + timeStr;
                activityList.add(createActivityItem(activity, false));
                count++;
            }
            
            if (count == 0) {
                JLabel noActivity = new JLabel("Belum ada aktivitas");
                noActivity.setFont(new Font("Arial", Font.ITALIC, 12));
                noActivity.setForeground(Color.GRAY);
                activityList.add(noActivity);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        activityList.revalidate();
        activityList.repaint();
    }
    
    private JPanel createActivityItem(String text, boolean isNew) {
        JPanel item = new JPanel(new BorderLayout(10, 5));
        item.setBackground(isNew ? new Color(255, 245, 245) : Color.WHITE);
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(8, 5, 8, 5)
        ));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        JLabel lblText = new JLabel("<html>" + text + "</html>");
        lblText.setFont(new Font("Arial", Font.PLAIN, 12));
        
        if (isNew) {
            JLabel badge = new JLabel("●");
            badge.setForeground(new Color(220, 53, 69));
            badge.setFont(new Font("Arial", Font.BOLD, 16));
            item.add(badge, BorderLayout.WEST);
        }
        
        item.add(lblText, BorderLayout.CENTER);
        
        return item;
    }
    
    private String getTimeAgo(Timestamp timestamp) {
        long diff = System.currentTimeMillis() - timestamp.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) return days + " hari lalu";
        if (hours > 0) return hours + " jam lalu";
        if (minutes > 0) return minutes + " menit lalu";
        return "Baru saja";
    }
    
    private void loadDashboardStatistics() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private int totalFakultas = 0;
            private int totalGedung = 0;
            private int totalReport = 0;
            private int totalJalan = 0;
            private int unreadReports = 0;
            private int activeClosures = 0;
            private int totalBuilding = 0;
            private int totalFasilitas = 0;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    System.out.println("=== Loading Dashboard Statistics ===");
                    
                    // Load Marker statistics (Fakultas & Gedung dari markers table)
                    MarkerDAO markerDAO = new MarkerDAO();
                    
                    // Hitung Fakultas dari markers dengan type "Fakultas"
                    List<Marker> fakultasList = markerDAO.getMarkersByType("Fakultas");
                    totalFakultas = fakultasList.size();
                    System.out.println("Total Fakultas: " + totalFakultas);
                    
                    // Hitung Gedung dari markers dengan type "Building"
                    List<Marker> gedungList = markerDAO.getMarkersByType("Gedung");
                    totalGedung = gedungList.size();
                    System.out.println("Total Gedung: " + totalGedung);
                    
                    // Hitung Building dari markers dengan type "Building"
                    List<Marker> buildingList = markerDAO.getMarkersByType("Building");
                    totalBuilding = buildingList.size();
                    System.out.println("Total Building: " + totalBuilding);
                    
                    // Hitung Fasilitas dari markers dengan type "Fasilitas"
                    List<Marker> fasilitasList = markerDAO.getMarkersByType("Fasilitas");
                    totalFasilitas = fasilitasList.size();
                    System.out.println("Total Fasilitas: " + totalFasilitas);
                    
                    // Load Report statistics
                    ReportDAO reportDAO = new ReportDAO();
                    List<Report> reportsList = reportDAO.getAllReports();
                    totalReport = reportsList.size();
                    unreadReports = reportDAO.getUnreadCount();
                    System.out.println("Total Reports: " + totalReport);
                    System.out.println("Unread Reports: " + unreadReports);
                    
                    // Load Road statistics - Total Jalan dari roads table
                    RoadDAO roadDAO = new RoadDAO();
                    List<Road> roadsList = roadDAO.getAllRoads();
                    totalJalan = roadsList.size();
                    System.out.println("Total Jalan: " + totalJalan);
                    
                    // Load Road Closure statistics
                    RoadClosureDAO closureDAO = new RoadClosureDAO();
                    List<RoadClosure> closuresList = closureDAO.getActiveClosures();
                    activeClosures = closuresList.size();
                    System.out.println("Active Closures: " + activeClosures);
                    
                    System.out.println("=== Statistics Loaded Successfully ===");
                    
                } catch (Exception e) {
                    System.err.println("Failed to load statistics: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void done() {
                lblTotalFakultas.setText(String.valueOf(totalFakultas));
                lblTotalGedung.setText(String.valueOf(totalGedung));
                lblTotalReport.setText(String.valueOf(totalReport));
                lblTotalMarker.setText(String.valueOf(totalJalan));
                lblUnreadReports.setText(String.valueOf(unreadReports));
                lblActiveClosures.setText(String.valueOf(activeClosures));
                lblTotalBuilding.setText(String.valueOf(totalBuilding));
                lblTotalFasilitas.setText(String.valueOf(totalFasilitas));
                
                // Load recent activities
                loadRecentActivities();
                
                // Refresh chart
                if (chartPanel != null) {
                    chartPanel.repaint();
                }
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
