-- ============================================
-- Database Schema untuk Navigasi USU
-- ============================================

CREATE DATABASE IF NOT EXISTS navigasi_usu;
USE navigasi_usu;

-- Tabel Users (untuk autentikasi)
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    nim VARCHAR(20) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    role ENUM('admin', 'user') DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabel Buildings (Gedung-gedung di USU)
CREATE TABLE IF NOT EXISTS buildings (
    building_id INT PRIMARY KEY AUTO_INCREMENT,
    building_code VARCHAR(20) UNIQUE NOT NULL,
    building_name VARCHAR(100) NOT NULL,
    building_type ENUM('fakultas', 'gedung', 'musholla', 'perpustakaan', 'stadion', 'masjid') NOT NULL,
    description TEXT,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    address TEXT,
    floor_count INT DEFAULT 1,
    icon_path VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_type (building_type),
    INDEX idx_active (is_active)
);

-- Tabel Rooms/Classes (Ruangan dan Kelas dalam gedung)
CREATE TABLE IF NOT EXISTS rooms (
    room_id INT PRIMARY KEY AUTO_INCREMENT,
    building_id INT NOT NULL,
    room_code VARCHAR(50) NOT NULL,
    room_name VARCHAR(100),
    floor_number INT NOT NULL,
    room_type ENUM('classroom', 'laboratory', 'office', 'auditorium', 'other') DEFAULT 'classroom',
    capacity INT,
    description TEXT,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (building_id) REFERENCES buildings(building_id) ON DELETE CASCADE,
    INDEX idx_building (building_id),
    INDEX idx_floor (floor_number)
);

-- Tabel Custom Markers (Marker custom yang ditambahkan admin)
CREATE TABLE IF NOT EXISTS markers (
    marker_id INT PRIMARY KEY AUTO_INCREMENT,
    marker_name VARCHAR(100) NOT NULL,
    marker_type VARCHAR(50) NOT NULL,
    description TEXT,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    icon_path VARCHAR(255),
    icon_name VARCHAR(100),
    created_by INT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_type (marker_type),
    INDEX idx_active (is_active)
);

-- Tabel Roads (Jalan-jalan di USU)
CREATE TABLE IF NOT EXISTS roads (
    road_id INT PRIMARY KEY AUTO_INCREMENT,
    road_name VARCHAR(100) NOT NULL,
    road_type ENUM('main', 'secondary', 'pedestrian') DEFAULT 'main',
    start_lat DECIMAL(10, 8) NOT NULL,
    start_lng DECIMAL(11, 8) NOT NULL,
    end_lat DECIMAL(10, 8) NOT NULL,
    end_lng DECIMAL(11, 8) NOT NULL,
    is_one_way BOOLEAN DEFAULT FALSE,
    direction ENUM('normal', 'reverse') DEFAULT 'normal',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_active (is_active)
);

-- Tabel Road Closures (Penutupan jalan)
CREATE TABLE IF NOT EXISTS road_closures (
    closure_id INT PRIMARY KEY AUTO_INCREMENT,
    road_id INT,
    closure_type ENUM('temporary', 'permanent', 'one_way') NOT NULL,
    reason VARCHAR(255),
    start_date DATE,
    end_date DATE,
    start_time TIME,
    end_time TIME,
    is_active BOOLEAN DEFAULT TRUE,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (road_id) REFERENCES roads(road_id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_active (is_active),
    INDEX idx_dates (start_date, end_date)
);

-- Tabel Facilities (Fasilitas tambahan)
CREATE TABLE IF NOT EXISTS facilities (
    facility_id INT PRIMARY KEY AUTO_INCREMENT,
    building_id INT,
    facility_name VARCHAR(100) NOT NULL,
    facility_type VARCHAR(50),
    description TEXT,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (building_id) REFERENCES buildings(building_id) ON DELETE CASCADE,
    INDEX idx_building (building_id)
);

-- Tabel Icon Uploads (Menyimpan metadata icon yang diupload)
CREATE TABLE IF NOT EXISTS icon_uploads (
    icon_id INT PRIMARY KEY AUTO_INCREMENT,
    original_filename VARCHAR(255) NOT NULL,
    saved_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size INT,
    mime_type VARCHAR(100),
    uploaded_by INT,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (uploaded_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- ============================================
-- Sample Data (Data Awal)
-- ============================================

-- Insert default admin user (password: admin123)
INSERT INTO users (nim, password, name, email, role) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrator', 'admin@usu.ac.id', 'admin'),
('2205181001', 'user123', 'User Example', 'user@usu.ac.id', 'user');

-- Insert sample buildings di USU
INSERT INTO buildings (building_code, building_name, building_type, description, latitude, longitude, address, floor_count, is_active) VALUES
('FK', 'Fakultas Kedokteran', 'fakultas', 'Fakultas Kedokteran Universitas Sumatera Utara', 3.5687890, 98.6562340, 'Jl. Dr. Mansur No.5, Padang Bulan', 4, TRUE),
('FT', 'Fakultas Teknik', 'fakultas', 'Fakultas Teknik Universitas Sumatera Utara', 3.5695420, 98.6548920, 'Jl. Almamater, Padang Bulan', 5, TRUE),
('FMIPA', 'Fakultas MIPA', 'fakultas', 'Fakultas Matematika dan Ilmu Pengetahuan Alam', 3.5701230, 98.6571450, 'Jl. Bioteknologi No.1, Padang Bulan', 4, TRUE),
('FH', 'Fakultas Hukum', 'fakultas', 'Fakultas Hukum Universitas Sumatera Utara', 3.5680560, 98.6551230, 'Jl. Universitas No.4, Padang Bulan', 3, TRUE),
('FEB', 'Fakultas Ekonomi dan Bisnis', 'fakultas', 'Fakultas Ekonomi dan Bisnis USU', 3.5692340, 98.6559870, 'Jl. Prof. T.M. Hanafiah, Padang Bulan', 4, TRUE),
('STADIUM', 'Stadion Mini USU', 'stadion', 'Stadion untuk kegiatan olahraga mahasiswa', 3.5698760, 98.6545120, 'Area Olahraga USU', 1, TRUE),
('PERPUS', 'Perpustakaan USU', 'perpustakaan', 'Perpustakaan Pusat Universitas Sumatera Utara', 3.5685430, 98.6563210, 'Jl. Universitas No.9, Padang Bulan', 3, TRUE),
('MASJID', 'Masjid Al-Makmur', 'masjid', 'Masjid kampus USU', 3.5676540, 98.6556780, 'Area Pusat Kampus USU', 2, TRUE);

-- Insert sample rooms
INSERT INTO rooms (building_id, room_code, room_name, floor_number, room_type, capacity) VALUES
(2, 'FT-101', 'Ruang Kuliah Teknik 1', 1, 'classroom', 50),
(2, 'FT-201', 'Lab Komputer 1', 2, 'laboratory', 40),
(2, 'FT-301', 'Ruang Seminar', 3, 'auditorium', 100),
(3, 'MIPA-101', 'Lab Fisika', 1, 'laboratory', 30),
(3, 'MIPA-201', 'Ruang Kuliah Matematika', 2, 'classroom', 45);

-- Insert sample roads
INSERT INTO roads (road_name, road_type, start_lat, start_lng, end_lat, end_lng, is_one_way, direction) VALUES
('Jl. Universitas', 'main', 3.5680000, 98.6550000, 3.5700000, 98.6560000, FALSE, 'normal'),
('Jl. Almamater', 'main', 3.5690000, 98.6545000, 3.5700000, 98.6565000, FALSE, 'normal'),
('Jl. Bioteknologi', 'secondary', 3.5695000, 98.6560000, 3.5705000, 98.6575000, FALSE, 'normal');

-- ============================================
-- Views untuk kemudahan query
-- ============================================

-- View untuk buildings dengan jumlah ruangan
CREATE VIEW v_buildings_summary AS
SELECT 
    b.building_id,
    b.building_code,
    b.building_name,
    b.building_type,
    b.latitude,
    b.longitude,
    COUNT(r.room_id) as total_rooms,
    b.is_active
FROM buildings b
LEFT JOIN rooms r ON b.building_id = r.building_id
GROUP BY b.building_id;

-- View untuk road closures yang aktif
CREATE VIEW v_active_closures AS
SELECT 
    rc.*,
    r.road_name,
    u.name as created_by_name
FROM road_closures rc
JOIN roads r ON rc.road_id = r.road_id
LEFT JOIN users u ON rc.created_by = u.user_id
WHERE rc.is_active = TRUE
AND (rc.end_date IS NULL OR rc.end_date >= CURDATE());

-- ============================================
-- Indexes untuk optimasi performa
-- ============================================

CREATE INDEX idx_buildings_location ON buildings(latitude, longitude);
CREATE INDEX idx_markers_location ON markers(latitude, longitude);
CREATE INDEX idx_roads_location ON roads(start_lat, start_lng, end_lat, end_lng);

-- ============================================
-- Grants (optional - untuk production)
-- ============================================

-- CREATE USER 'navigasi_user'@'localhost' IDENTIFIED BY 'usu_navigate_2024';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON navigasi_usu.* TO 'navigasi_user'@'localhost';
-- FLUSH PRIVILEGES;
