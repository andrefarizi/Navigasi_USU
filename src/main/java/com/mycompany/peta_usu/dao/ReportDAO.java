package com.mycompany.peta_usu.dao;

import com.mycompany.peta_usu.config.DatabaseConnection;
import com.mycompany.peta_usu.models.Report;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for managing user reports
 */
public class ReportDAO {
    
    /**
     * Create new report
     */
    public boolean createReport(Report report) {
        String sql = "INSERT INTO reports (user_nim, user_name, location, latitude, longitude, description, report_type, created_at, is_read) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), FALSE)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, report.getUserNim());
            stmt.setString(2, report.getUserName());
            stmt.setString(3, report.getLocation());
            stmt.setDouble(4, report.getLatitude());
            stmt.setDouble(5, report.getLongitude());
            stmt.setString(6, report.getDescription());
            stmt.setString(7, report.getReportType().name()); // Gunakan name() bukan toString()
            
            int affected = stmt.executeUpdate();
            return affected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all reports
     */
    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Report report = new Report();
                report.setReportId(rs.getInt("report_id"));
                report.setUserNim(rs.getString("user_nim"));
                report.setUserName(rs.getString("user_name"));
                report.setLocation(rs.getString("location"));
                report.setLatitude(rs.getDouble("latitude"));
                report.setLongitude(rs.getDouble("longitude"));
                report.setDescription(rs.getString("description"));
                report.setReportType(Report.ReportType.valueOf(rs.getString("report_type")));
                report.setCreatedAt(rs.getTimestamp("created_at"));
                report.setRead(rs.getBoolean("is_read"));
                
                reports.add(report);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return reports;
    }
    
    /**
     * Get unread report count
     */
    public int getUnreadCount() {
        String sql = "SELECT COUNT(*) FROM reports WHERE is_read = FALSE";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Mark report as read
     */
    public boolean markAsRead(int reportId) {
        String sql = "UPDATE reports SET is_read = TRUE WHERE report_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, reportId);
            int affected = stmt.executeUpdate();
            return affected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete report by ID
     */
    public boolean deleteReport(int reportId) {
        String sql = "DELETE FROM reports WHERE report_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, reportId);
            int affected = stmt.executeUpdate();
            return affected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
