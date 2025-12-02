-- Create reports table for user road condition reports
USE navigasi_usu;

CREATE TABLE IF NOT EXISTS reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    user_nim VARCHAR(20),
    user_name VARCHAR(100),
    location VARCHAR(255),
    latitude DOUBLE,
    longitude DOUBLE,
    description TEXT,
    report_type ENUM('JALAN_RUSAK', 'JALAN_TERTUTUP', 'RAMBU_HILANG', 'LAINNYA') DEFAULT 'LAINNYA',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    
    INDEX idx_created_at (created_at),
    INDEX idx_is_read (is_read),
    INDEX idx_user_nim (user_nim)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
