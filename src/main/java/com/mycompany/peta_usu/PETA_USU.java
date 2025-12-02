/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.peta_usu;

import javax.swing.*;

/**
 * PETA_USU - Main entry point
 * User langsung ke MapFrame (no login)
 * Admin bisa akses via LoginFrame terpisah
 * 
 * @author PETA_USU Team
 */
public class PETA_USU {

    public static void main(String[] args) {
        // Set Look and Feel
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Gagal set LookAndFeel: " + e.getMessage());
        }

        // Show splash screen dengan pilihan
        SwingUtilities.invokeLater(() -> {
            showWelcomeScreen();
        });
    }
    
    private static void showWelcomeScreen() {
        JFrame welcomeFrame = new JFrame("PetaUSU - Navigasi Kampus USU");
        welcomeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        welcomeFrame.setSize(600, 480);
        welcomeFrame.setLocationRelativeTo(null);
        welcomeFrame.setResizable(false);
        
        // Main panel with gradient effect
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                java.awt.GradientPaint gradient = new java.awt.GradientPaint(
                    0, 0, new java.awt.Color(34, 139, 34),
                    0, getHeight(), new java.awt.Color(56, 136, 96)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 60, 50, 60));
        
        // Logo/Icon placeholder
        JLabel iconLabel = new JLabel("🎓");
        iconLabel.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 60));
        iconLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        panel.add(iconLabel);
        panel.add(Box.createVerticalStrut(15));
        
        // Title with shadow effect
        JLabel titleLabel = new JLabel("Selamat Datang di PetaUSU");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 28));
        titleLabel.setForeground(java.awt.Color.WHITE);
        titleLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Sistem Navigasi Kampus USU");
        subtitleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 16));
        subtitleLabel.setForeground(new java.awt.Color(240, 255, 240));
        subtitleLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(subtitleLabel);
        panel.add(Box.createVerticalStrut(40));
        
        // User button with modern styling
        JButton userButton = new JButton("🗺️  Lihat Peta Kampus");
        userButton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        userButton.setBackground(new java.awt.Color(76, 175, 80));
        userButton.setForeground(java.awt.Color.WHITE);
        userButton.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        userButton.setMaximumSize(new java.awt.Dimension(350, 55));
        userButton.setFocusPainted(false);
        userButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new java.awt.Color(67, 160, 71), 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        userButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        // Hover effect
        userButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                userButton.setBackground(new java.awt.Color(67, 160, 71));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                userButton.setBackground(new java.awt.Color(76, 175, 80));
            }
        });
        
        userButton.addActionListener(e -> {
            welcomeFrame.dispose();
            try {
                MapFrame mapFrame = new MapFrame("guest");
                mapFrame.setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, 
                    "Error membuka peta: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        panel.add(userButton);
        panel.add(Box.createVerticalStrut(18));
        
        // Admin button with modern styling
        JButton adminButton = new JButton("🔐  Login Admin");
        adminButton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        adminButton.setBackground(new java.awt.Color(33, 150, 243));
        adminButton.setForeground(java.awt.Color.WHITE);
        adminButton.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        adminButton.setMaximumSize(new java.awt.Dimension(350, 50));
        adminButton.setFocusPainted(false);
        adminButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new java.awt.Color(25, 118, 210), 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        adminButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        // Hover effect
        adminButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                adminButton.setBackground(new java.awt.Color(25, 118, 210));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                adminButton.setBackground(new java.awt.Color(33, 150, 243));
            }
        });
        
        adminButton.addActionListener(e -> {
            welcomeFrame.dispose();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
        
        panel.add(adminButton);
        panel.add(Box.createVerticalStrut(25));
        
        // Info label with better styling and proper spacing
        JLabel infoLabel = new JLabel("<html><div style='text-align: center; line-height: 1.5;'>" +
            "👥 <b>User</b> dapat langsung melihat peta<br>" +
            "🔑 <b>Admin</b> perlu login untuk akses panel" +
            "</div></html>");
        infoLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        infoLabel.setForeground(new java.awt.Color(240, 255, 240));
        infoLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        infoLabel.setMaximumSize(new java.awt.Dimension(400, 60));
        panel.add(infoLabel);
        
        welcomeFrame.add(panel);
        welcomeFrame.setVisible(true);
    }
}
