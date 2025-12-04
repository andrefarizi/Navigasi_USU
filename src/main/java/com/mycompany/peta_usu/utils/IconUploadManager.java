package com.mycompany.peta_usu.utils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * IconUploadManager - Utility untuk upload dan manage icon
 * Fitur untuk admin upload icon dari laptop
 * 
 * === 4 PILAR OOP YANG DITERAPKAN ===
 * 
 * 1. ENCAPSULATION (Enkapsulasi):
 *    - Constants (UPLOAD_DIRECTORY, MAX_FILE_SIZE) PRIVATE STATIC FINAL
 *    - Method validateFile() PRIVATE (internal validation only)
 *    - Tujuan: Sembunyikan detail validasi dan path configuration
 * 
 * 2. POLYMORPHISM (Polimorfisme):
 *    - Method uploadIcon() bisa terima File dari berbagai source
 *    - JFileChooser.showOpenDialog() polymorphic (bisa null parent atau JFrame)
 * 
 * 3. ABSTRACTION (Abstraksi):
 *    - Utility abstraksi lengkap dari file upload process
 *    - Sembunyikan: File I/O, path manipulation, UUID generation, file copy
 *    - User cukup: selectIconFile(frame) → dapat File
 *    - uploadIcon(file) → dapat path hasil upload
 *    - Tidak perlu tahu: Files.copy(), StandardCopyOption, Path, UUID
 * 
 * 4. STATIC METHODS:
 *    - Semua method STATIC (utility class pattern)
 *    - Tidak perlu instance: IconUploadManager.selectIconFile()
 * 
 * @author PETA_USU Team
 */
public class IconUploadManager {
    
    // === ENCAPSULATION: Constants PRIVATE ===
    
    private static final Logger logger = Logger.getLogger(IconUploadManager.class.getName());
    private static final String UPLOAD_DIRECTORY = "resources/icons/";
    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {"png", "jpg", "jpeg", "gif", "svg"};
    
    /**
     * Open file chooser untuk pilih icon
     */
    public static File selectIconFile(JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Pilih Icon");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        // Filter file type
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image Files (*.png, *.jpg, *.jpeg, *.gif, *.svg)", 
            ALLOWED_EXTENSIONS
        );
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(parent);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Validate file
            if (validateFile(selectedFile)) {
                return selectedFile;
            } else {
                JOptionPane.showMessageDialog(parent, 
                    "File tidak valid atau terlalu besar (max 5MB)", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Validate file size dan extension
     */
    private static boolean validateFile(File file) {
        // Check file size
        if (file.length() > MAX_FILE_SIZE) {
            logger.warning("File too large: " + file.length());
            return false;
        }
        
        // Check extension
        String fileName = file.getName().toLowerCase();
        boolean validExtension = false;
        
        for (String ext : ALLOWED_EXTENSIONS) {
            if (fileName.endsWith("." + ext)) {
                validExtension = true;
                break;
            }
        }
        
        if (!validExtension) {
            logger.warning("Invalid file extension: " + fileName);
            return false;
        }
        
        return true;
    }
    
    /**
     * Upload icon ke server/local storage
     * Return path file yang disimpan
     */
    public static String uploadIcon(File sourceFile) {
        try {
            // Create upload directory if not exists
            Path uploadDir = Paths.get(UPLOAD_DIRECTORY);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // Generate unique filename
            String extension = getFileExtension(sourceFile.getName());
            String uniqueFileName = UUID.randomUUID().toString() + "." + extension;
            
            // Destination path
            Path destination = uploadDir.resolve(uniqueFileName);
            
            // Copy file
            Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("Icon uploaded successfully: " + uniqueFileName);
            
            return destination.toString();
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error uploading icon", e);
            return null;
        }
    }
    
    /**
     * Upload icon dengan resize
     */
    public static String uploadIconWithResize(File sourceFile, int maxWidth, int maxHeight) {
        try {
            // Read original image
            BufferedImage originalImage = ImageIO.read(sourceFile);
            
            if (originalImage == null) {
                logger.warning("Cannot read image file");
                return null;
            }
            
            // Calculate new dimensions
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            double widthRatio = (double) maxWidth / originalWidth;
            double heightRatio = (double) maxHeight / originalHeight;
            double ratio = Math.min(widthRatio, heightRatio);
            
            int newWidth = (int) (originalWidth * ratio);
            int newHeight = (int) (originalHeight * ratio);
            
            // Resize image
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g.dispose();
            
            // Save resized image
            Path uploadDir = Paths.get(UPLOAD_DIRECTORY);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            String extension = getFileExtension(sourceFile.getName());
            String uniqueFileName = UUID.randomUUID().toString() + "." + extension;
            Path destination = uploadDir.resolve(uniqueFileName);
            
            ImageIO.write(resizedImage, extension, destination.toFile());
            
            logger.info("Icon uploaded and resized successfully: " + uniqueFileName);
            
            return destination.toString();
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error uploading and resizing icon", e);
            return null;
        }
    }
    
    /**
     * Delete icon file
     */
    public static boolean deleteIcon(String iconPath) {
        try {
            Path path = Paths.get(iconPath);
            
            if (Files.exists(path)) {
                Files.delete(path);
                logger.info("Icon deleted successfully: " + iconPath);
                return true;
            }
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error deleting icon", e);
        }
        
        return false;
    }
    
    /**
     * Get file extension
     */
    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * Get ImageIcon from path
     */
    public static ImageIcon getIconFromPath(String iconPath) {
        try {
            if (iconPath != null && !iconPath.isEmpty()) {
                File iconFile = new File(iconPath);
                if (iconFile.exists()) {
                    return new ImageIcon(iconPath);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error loading icon", e);
        }
        
        return null;
    }
    
    /**
     * Get file size in MB
     */
    public static double getFileSizeMB(File file) {
        return (double) file.length() / (1024 * 1024);
    }
}
