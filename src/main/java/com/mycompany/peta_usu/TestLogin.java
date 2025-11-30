package com.mycompany.peta_usu;

import com.mycompany.peta_usu.middleware.AuthMiddleware;
import javax.swing.*;

/**
 * TestLogin - Simple test untuk login functionality
 */
public class TestLogin {
    
    public static void main(String[] args) {
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Langsung buka LoginFrame
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
