package com.mycompany.peta_usu.middleware;

import com.mycompany.peta_usu.dao.UserDAO;
import com.mycompany.peta_usu.models.User;

/**
 * AuthMiddleware - Middleware untuk autentikasi dan authorization
 * Memastikan hanya admin yang bisa login
 * 
 * === 4 PILAR OOP YANG DITERAPKAN ===
 * 
 * 1. ENCAPSULATION (Enkapsulasi):
 *    - userDAO dan currentUser PRIVATE STATIC
 *    - Hanya bisa diakses via method public (authenticateAdmin, getCurrentUser)
 *    - Tujuan: Kontrol penuh terhadap session management
 * 
 * 2. POLYMORPHISM (Polimorfisme):
 *    - requireAdmin() throw SecurityException (polymorphic exception handling)
 *    - isAdmin() return boolean (bisa dipakai di if, while, ternary, dll)
 * 
 * 3. ABSTRACTION (Abstraksi):
 *    - Middleware abstraksi lengkap dari authentication system
 *    - Sembunyikan: UserDAO, database query, session storage
 *    - User cukup: AuthMiddleware.authenticateAdmin(user, pass)
 *    - Hasil: true/false, auto simpan currentUser di memory
 *    - getCurrentUser() tanpa tahu bagaimana user disimpan
 * 
 * 4. STATIC METHODS:
 *    - Semua method STATIC (tidak perlu new AuthMiddleware())
 *    - Cocok untuk singleton pattern: 1 user login per aplikasi
 * 
 * @author PETA_USU Team
 */
public class AuthMiddleware {
    
    // === ENCAPSULATION: Field PRIVATE STATIC ===
    // Singleton pattern untuk session management
    
    private static UserDAO userDAO = new UserDAO();
    private static User currentUser = null;
    
    /**
     * Authenticate user (hanya untuk admin)
     */
    public static boolean authenticateAdmin(String username, String password) {
        User user = userDAO.authenticate(username, password);
        
        if (user != null && user.getRole() == User.UserRole.ADMIN) {
            currentUser = user;
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if user is admin
     */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == User.UserRole.ADMIN;
    }
    
    /**
     * Get current logged in user
     */
    public static User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Logout user
     */
    public static void logout() {
        currentUser = null;
    }
    
    /**
     * Require admin access - throw exception if not admin
     */
    public static void requireAdmin() throws SecurityException {
        if (!isAdmin()) {
            throw new SecurityException("Access denied. Admin privileges required.");
        }
    }
}
