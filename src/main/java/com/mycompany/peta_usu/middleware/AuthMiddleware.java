package com.mycompany.peta_usu.middleware;

import com.mycompany.peta_usu.dao.UserDAO;
import com.mycompany.peta_usu.models.User;

/**
 * AuthMiddleware - Middleware untuk autentikasi dan authorization
 * Memastikan hanya admin yang bisa login
 * 
 * @author PETA_USU Team
 */
public class AuthMiddleware {
    
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
