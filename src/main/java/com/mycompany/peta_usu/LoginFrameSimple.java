package com.mycompany.peta_usu;

import com.mycompany.peta_usu.middleware.AuthMiddleware;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import java.io.File;

/**
 * LoginFrameSimple - Login HANYA untuk Admin (Simplified untuk testing)
 */
public class LoginFrameSimple extends javax.swing.JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton backButton;
    private JCheckBox showPassword;
    private Image backgroundImage;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LoginFrameSimple.class.getName());
    private final Color greenUSU = new Color(0x38, 0x88, 0x60);
    
    public LoginFrameSimple() {
        initComponents();
        setupLoginUI();
    }
    
    private void setupLoginUI() {
        setTitle("Admin Login - Peta USU");
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
        loginPanel.setSize(320, 340);
        loginPanel.setBorder(BorderFactory.createLineBorder(greenUSU, 2));

        JLabel titleLabel = new JLabel("Admin Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 24));
        titleLabel.setForeground(greenUSU);
        titleLabel.setBounds(0, 15, 320, 30);

        JLabel subtitleLabel = new JLabel("Peta USU Management", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        subtitleLabel.setBounds(0, 45, 320, 20);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        usernameLabel.setBounds(40, 85, 240, 20);
        usernameField = new JTextField();
        usernameField.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        usernameField.setBounds(40, 105, 240, 28);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        passwordLabel.setBounds(40, 140, 240, 20);
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        passwordField.setBounds(40, 160, 240, 28);

        showPassword = new JCheckBox("Show Password");
        showPassword.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        showPassword.setBackground(new Color(255, 255, 255, 0));
        showPassword.setBounds(40, 195, 240, 20);
        showPassword.addActionListener(e -> {
            passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '\u2022');
        });

        loginButton = new JButton("Login as Admin");
        loginButton.setFont(new Font("Times New Roman", Font.BOLD, 15));
        loginButton.setBounds(40, 230, 240, 35);
        loginButton.setBackground(greenUSU);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> login());

        backButton = new JButton("← Kembali");
        backButton.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        backButton.setBounds(40, 275, 240, 30);
        backButton.setBackground(new Color(200, 200, 200));
        backButton.setForeground(Color.BLACK);
        backButton.setFocusPainted(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> {
            System.exit(0);
        });

        loginPanel.add(titleLabel);
        loginPanel.add(subtitleLabel);
        loginPanel.add(usernameLabel);
        loginPanel.add(usernameField);
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordField);
        loginPanel.add(showPassword);
        loginPanel.add(loginButton);
        loginPanel.add(backButton);

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
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Username dan Password harus diisi!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Gunakan middleware untuk autentikasi
        if (AuthMiddleware.authenticateAdmin(username, password)) {
            JOptionPane.showMessageDialog(this,
                "Login Berhasil!\n\nSelamat datang, " + AuthMiddleware.getCurrentUser().getName() + "!\n\nAdmin panel belum selesai.\nUntuk saat ini, login berhasil dikonfirmasi.",
                "Login Successful",
                JOptionPane.INFORMATION_MESSAGE);
            
            // TODO: Buka utamaadmin setelah semua UI selesai
            // utamaadmin adminFrame = new utamaadmin();
            // adminFrame.setVisible(true);
            // this.dispose();
                    
        } else {
            JOptionPane.showMessageDialog(this,
                "Username atau password salah!\nAtau Anda bukan admin.",
                "Login Gagal",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
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
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        java.awt.EventQueue.invokeLater(() -> new LoginFrameSimple().setVisible(true));
    }
}
