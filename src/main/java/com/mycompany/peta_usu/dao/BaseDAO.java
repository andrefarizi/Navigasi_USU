package com.mycompany.peta_usu.dao;

import com.mycompany.peta_usu.config.DatabaseConnection;
import com.mycompany.peta_usu.services.CRUDService;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BaseDAO - Abstract base class untuk semua DAO
 * Implementasi Inheritance, Abstraction & Polymorphism
 * 
 * Menyediakan:
 * - Database connection management
 * - Common CRUD operations template
 * - Resource cleanup utilities
 * 
 * @param <T> Type of entity
 * @author PETA_USU Team
 */
public abstract class BaseDAO<T> implements CRUDService<T> {
    
    protected final Connection connection;
    protected final Logger logger;
    
    /**
     * Constructor - Initialize connection and logger
     */
    public BaseDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
        this.logger = Logger.getLogger(this.getClass().getName());
    }
    
    /**
     * Get table name - must be implemented by subclass
     * @return Table name in database
     */
    protected abstract String getTableName();
    
    /**
     * Get primary key column name
     * @return Primary key column name
     */
    protected abstract String getPrimaryKeyColumn();
    
    /**
     * Map ResultSet to Entity object
     * @param rs ResultSet from query
     * @return Entity object
     * @throws SQLException if mapping fails
     */
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;
    
    /**
     * Close database resources safely
     * Polymorphism: Multiple overloaded versions
     */
    protected void closeResources(ResultSet rs, Statement stmt) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error closing resources", e);
        }
    }
    
    /**
     * Overloaded version - close statement only
     */
    protected void closeResources(Statement stmt) {
        closeResources(null, stmt);
    }
    
    /**
     * Overloaded version - close result set only
     */
    protected void closeResources(ResultSet rs) {
        closeResources(rs, null);
    }
    
    /**
     * Generic method to execute SELECT query and get single result
     * @param sql SQL query
     * @param params Query parameters
     * @return Entity or null
     */
    protected T executeQuerySingle(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, params);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error executing query: " + sql, e);
        }
        
        return null;
    }
    
    /**
     * Set parameters to PreparedStatement
     * @param pstmt PreparedStatement
     * @param params Parameters to set
     * @throws SQLException if setting fails
     */
    protected void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
    
    /**
     * Log operation
     * @param operation Operation name
     * @param success Success status
     */
    protected void logOperation(String operation, boolean success) {
        if (success) {
            logger.info(operation + " successful on " + getTableName());
        } else {
            logger.warning(operation + " failed on " + getTableName());
        }
    }
    
    /**
     * Count total records (implements interface method)
     * @return Total count
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE is_active = TRUE";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error counting records", e);
        }
        
        return 0;
    }
}
