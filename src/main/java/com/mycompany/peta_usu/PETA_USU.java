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
        welcomeFrame.setSize(500, 350);
        welcomeFrame.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        panel.setBackground(new java.awt.Color(56, 136, 96));
        
        // Title
        JLabel titleLabel = new JLabel("Selamat Datang di PetaUSU");
        titleLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
        titleLabel.setForeground(java.awt.Color.WHITE);
        titleLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Sistem Navigasi Kampus USU");
        subtitleLabel.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 16));
        subtitleLabel.setForeground(new java.awt.Color(220, 255, 220));
        subtitleLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subtitleLabel);
        panel.add(Box.createVerticalStrut(50));
        
        // User button (default - langsung ke map)
        JButton userButton = new JButton("🗺️  Lihat Peta Kampus");
        userButton.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
        userButton.setBackground(new java.awt.Color(76, 175, 80));
        userButton.setForeground(java.awt.Color.WHITE);
        userButton.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        userButton.setMaximumSize(new java.awt.Dimension(300, 50));
        userButton.setFocusPainted(false);
        userButton.addActionListener(e -> {
            welcomeFrame.dispose();
            // Langsung ke MapFrame tanpa login
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
        panel.add(Box.createVerticalStrut(20));
        
        // Admin button
        JButton adminButton = new JButton("🔐  Login Admin");
        adminButton.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
        adminButton.setBackground(new java.awt.Color(33, 150, 243));
        adminButton.setForeground(java.awt.Color.WHITE);
        adminButton.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        adminButton.setMaximumSize(new java.awt.Dimension(300, 45));
        adminButton.setFocusPainted(false);
        adminButton.addActionListener(e -> {
            welcomeFrame.dispose();
            // Ke LoginFrame untuk admin
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
        
        panel.add(adminButton);
        panel.add(Box.createVerticalStrut(30));
        
        // Info label
        JLabel infoLabel = new JLabel("<html><center>User dapat langsung melihat peta<br>Admin perlu login untuk akses panel admin</center></html>");
        infoLabel.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 11));
        infoLabel.setForeground(new java.awt.Color(220, 255, 220));
        infoLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        panel.add(infoLabel);
        
        welcomeFrame.add(panel);
        welcomeFrame.setVisible(true);
    }
}
