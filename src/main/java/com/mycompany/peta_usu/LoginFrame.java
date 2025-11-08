/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.peta_usu;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

/**
 *
 * @author ASUS
 */
public class LoginFrame extends javax.swing.JFrame {
    private JTextField nimField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JCheckBox showPassword;
    private JLabel forgotPasswordLabel;
    private Image backgroundImage;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LoginFrame.class.getName());
    private final Color greenUSU = new Color(0x38, 0x88, 0x60);
    
    
    /**
     * Creates new form LoginFrame
     */
    public LoginFrame() {
        initComponents();
        setupLoginUI();
    }
    
    private void setupLoginUI() {
        setTitle("Login - Peta USU");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            backgroundImage = ImageIO.read(new File("resources/biro-usu.jpg"));
            logger.info("Background image loaded successfully");
        } catch (Exception e) {
            logger.warning("Background image not found: " + e.getMessage());
        }

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor(new Color(255, 255, 255, 90));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.dispose();
                } else {
                    g.setColor(greenUSU);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setLayout(null);

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(null);
        loginPanel.setBackground(new Color(255, 255, 255, 230));
        loginPanel.setSize(320, 310);
        loginPanel.setBorder(BorderFactory.createLineBorder(greenUSU, 2));

        JLabel titleLabel = new JLabel("Login Peta USU", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 22));
        titleLabel.setForeground(greenUSU);
        titleLabel.setBounds(0, 20, 320, 30);

        JLabel nimLabel = new JLabel("NIM Mahasiswa:");
        nimLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        nimLabel.setBounds(40, 70, 240, 20);
        nimField = new JTextField();
        nimField.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        nimField.setBounds(40, 90, 240, 28);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        passwordLabel.setBounds(40, 125, 240, 20);
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        passwordField.setBounds(40, 145, 240, 28);

        showPassword = new JCheckBox("Show Password");
        showPassword.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        showPassword.setBackground(new Color(255, 255, 255, 0));
        showPassword.setBounds(40, 180, 240, 20);
        showPassword.addActionListener(e -> {
            passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '\u2022');
        });

        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Times New Roman", Font.BOLD, 15));
        loginButton.setBounds(40, 210, 240, 35);
        loginButton.setBackground(greenUSU);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> login());

        forgotPasswordLabel = new JLabel("Forgot Password?");
        forgotPasswordLabel.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        forgotPasswordLabel.setForeground(new Color(0, 51, 204));
        forgotPasswordLabel.setBounds(170, 255, 120, 20);
        forgotPasswordLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(LoginFrame.this,
                        "Silakan hubungi admin untuk reset password.",
                        "Lupa Password",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                forgotPasswordLabel.setText("<html><u>Forgot Password?</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                forgotPasswordLabel.setText("Forgot Password?");
            }
        });

        loginPanel.add(titleLabel);
        loginPanel.add(nimLabel);
        loginPanel.add(nimField);
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordField);
        loginPanel.add(showPassword);
        loginPanel.add(loginButton);
        loginPanel.add(forgotPasswordLabel);

        Runnable centerPanel = () -> {
            int x = (mainPanel.getWidth() - loginPanel.getWidth()) / 2;
            int y = (mainPanel.getHeight() - loginPanel.getHeight()) / 2;
            loginPanel.setLocation(Math.max(x, 0), Math.max(y, 0));
        };
        mainPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                centerPanel.run();
            }
        });
        centerPanel.run();

        mainPanel.add(loginPanel);
        setContentPane(mainPanel);
    }
    
    private void login() {
        String nim = nimField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (nim.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "NIM dan Password harus diisi!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (nim.equals("2205181001") && password.equals("12345")) {
            try {
                MapFrame mapFrame = new MapFrame(nim);
                mapFrame.setVisible(true);
                this.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error membuka peta: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                logger.severe("Error opening MapFrame: " + ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Akun tidak ditemukan!",
                    "Login Gagal",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
