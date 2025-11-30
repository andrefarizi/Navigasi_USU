-- ============================================
-- NAVIGASI USU - Complete Database Script
-- Sistem Navigasi Kampus Universitas Sumatera Utara
-- ============================================

-- ============================================
-- 1. DATABASE SETUP
-- ============================================

CREATE DATABASE IF NOT EXISTS navigasi_usu;
USE navigasi_usu;

-- ============================================
-- 2. DROP EXISTING TABLES (if needed)
-- ============================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS road_closures;
DROP TABLE IF EXISTS roads;
DROP TABLE IF EXISTS icon_uploads;
DROP TABLE IF EXISTS facilities;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS markers;
DROP TABLE IF EXISTS buildings;
DROP TABLE IF EXISTS users;

-- Drop views
DROP VIEW IF EXISTS v_buildings_summary;
DROP VIEW IF EXISTS v_active_closures;

-- Drop procedures
DROP PROCEDURE IF EXISTS sp_get_active_markers;
DROP PROCEDURE IF EXISTS sp_get_buildings_by_type;
DROP PROCEDURE IF EXISTS sp_check_road_status;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 3. CREATE TABLES
-- ============================================

-- Tabel Users (untuk autentikasi)
CREATE TABLE users (
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
CREATE TABLE buildings (
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
CREATE TABLE rooms (
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
CREATE TABLE markers (
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
CREATE TABLE roads (
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
CREATE TABLE road_closures (
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
CREATE TABLE facilities (
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
CREATE TABLE icon_uploads (
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
-- 4. CREATE VIEWS
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
WHERE rc.is_active = TRUE;

-- ============================================
-- 5. INSERT DEFAULT DATA
-- ============================================

-- Insert default admin user (password: admin123)
INSERT INTO users (nim, password, name, email, role) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrator', 'admin@usu.ac.id', 'admin'),
('2205181001', 'user123', 'User Example', 'user@usu.ac.id', 'user');

-- ============================================
-- 6. INSERT COMPLETE USU DATA
-- ============================================

-- Insert Buildings (Gedung-gedung di USU)
INSERT INTO buildings (building_code, building_name, building_type, description, latitude, longitude, address, floor_count, is_active) VALUES
('REKTORAT', 'Gedung Rektorat USU', 'gedung', 'Gedung Rektorat Universitas Sumatera Utara', 3.5668, 98.6588, 'Jl. Universitas No. 1', 3, 1),
('PERPUS', 'Perpustakaan Universitas', 'perpustakaan', 'Perpustakaan Pusat USU', 3.5665, 98.6587, 'Kampus USU', 3, 1),
('FMIPA', 'Fakultas MIPA', 'fakultas', 'Gedung Fakultas Matematika dan Ilmu Pengetahuan Alam', 3.5675, 98.6585, 'Kampus USU', 4, 1),
('FT', 'Fakultas Teknik', 'fakultas', 'Gedung Fakultas Teknik USU', 3.5685, 98.6587, 'Kampus USU', 5, 1),
('BIOTE', 'Gedung Bioteknologi', 'gedung', 'Gedung Bioteknologi USU', 3.5665, 98.6593, 'Kampus USU', 3, 1),
('STADIUM', 'Stadium Mini USU', 'stadion', 'Stadion Mini untuk olahraga mahasiswa', 3.5675, 98.6603, 'Kampus USU', 1, 1),
('MASJID', 'Masjid Al-Makmur USU', 'masjid', 'Masjid untuk mahasiswa dan staf USU', 3.5660, 98.6585, 'Kampus USU', 2, 1),
('AUDITORIUM', 'Auditorium USU', 'gedung', 'Auditorium Universitas Sumatera Utara', 3.5663, 98.6588, 'Kampus USU', 2, 1),
('POLTEK', 'Politeknik Negeri Medan', 'gedung', 'Politeknik Negeri Medan (dalam area USU)', 3.5660, 98.6572, 'Kampus USU', 3, 1),
('GERBANG', 'Gerbang Utama USU', 'gedung', 'Pintu masuk utama kampus USU', 3.5650, 98.6576, 'Jl. Alumni', 1, 1);

-- Insert Markers (Lokasi penting di USU)
INSERT INTO markers (marker_name, marker_type, description, latitude, longitude, is_active) VALUES
('Parkir Gedung Rektorat', 'parking', 'Area parkir gedung rektorat', 3.5667, 98.6590, 1),
('Kantin Fakultas MIPA', 'kantin', 'Kantin untuk mahasiswa MIPA', 3.5676, 98.6583, 1),
('Taman Kampus', 'taman', 'Taman untuk refreshing mahasiswa', 3.5672, 98.6586, 1),
('Halte Bus Kampus', 'transportasi', 'Halte bus untuk transportasi mahasiswa', 3.5652, 98.6578, 1),
('ATM Center', 'fasilitas', 'ATM Center untuk mahasiswa', 3.5662, 98.6584, 1);

-- Insert Roads (Jalan-jalan di USU)
INSERT INTO roads (road_name, road_type, start_lat, start_lng, end_lat, end_lng, is_one_way, direction, is_active) VALUES
-- Jalan Utama
('Jl. Alumni (Gerbang Utama)', 'main', 3.5650, 98.6575, 3.5660, 98.6580, 0, 'normal', 1),
('Jl. Alumni (Tengah)', 'main', 3.5660, 98.6580, 3.5670, 98.6585, 0, 'normal', 1),
('Jl. Alumni (Dalam)', 'main', 3.5670, 98.6585, 3.5680, 98.6590, 0, 'normal', 1),

-- Jalan Sekunder
('Jl. Bioteknologi 1', 'secondary', 3.5660, 98.6590, 3.5670, 98.6595, 0, 'normal', 1),
('Jl. Bioteknologi 2', 'secondary', 3.5670, 98.6595, 3.5680, 98.6600, 0, 'normal', 1),
('Jl. USU Utara', 'secondary', 3.5680, 98.6590, 3.5690, 98.6595, 0, 'normal', 1),
('Jl. USU Tengah', 'secondary', 3.5670, 98.6585, 3.5680, 98.6590, 0, 'normal', 1),
('Jl. USU Selatan', 'secondary', 3.5660, 98.6580, 3.5670, 98.6585, 0, 'normal', 1),
('Jl. Perpustakaan', 'secondary', 3.5665, 98.6582, 3.5665, 98.6592, 0, 'normal', 1),
('Jl. FMIPA', 'secondary', 3.5675, 98.6580, 3.5675, 98.6590, 0, 'normal', 1),
('Jl. FT Utara', 'secondary', 3.5685, 98.6585, 3.5690, 98.6590, 0, 'normal', 1),
('Jl. FT Selatan', 'secondary', 3.5680, 98.6585, 3.5685, 98.6590, 0, 'normal', 1),
('Jl. Politeknik', 'main', 3.5655, 98.6570, 3.5665, 98.6575, 0, 'normal', 1),
('Jl. Stadium', 'secondary', 3.5670, 98.6600, 3.5680, 98.6605, 0, 'normal', 1),
('Jl. Rektorat', 'secondary', 3.5668, 98.6588, 3.5672, 98.6592, 0, 'normal', 1),
('Jl. Auditorium', 'secondary', 3.5662, 98.6586, 3.5665, 98.6590, 0, 'normal', 1),
('Jl. Mesjid', 'secondary', 3.5658, 98.6583, 3.5662, 98.6586, 0, 'normal', 1),

-- Jalan Penghubung
('Jl. Penghubung 1', 'secondary', 3.5665, 98.6580, 3.5670, 98.6582, 0, 'normal', 1),
('Jl. Penghubung 2', 'secondary', 3.5670, 98.6582, 3.5675, 98.6585, 0, 'normal', 1),
('Jl. Penghubung 3', 'secondary', 3.5675, 98.6587, 3.5680, 98.6589, 0, 'normal', 1),
('Jl. Lingkar Kampus', 'secondary', 3.5655, 98.6575, 3.5660, 98.6580, 0, 'normal', 1);

-- Insert sample rooms
INSERT INTO rooms (building_id, room_code, room_name, floor_number, room_type, capacity) VALUES
(2, 'PERPUS-101', 'Ruang Baca Utama', 1, 'other', 100),
(2, 'PERPUS-201', 'Ruang Koleksi', 2, 'other', 50),
(3, 'MIPA-101', 'Lab Fisika', 1, 'laboratory', 30),
(3, 'MIPA-201', 'Ruang Kuliah Matematika', 2, 'classroom', 45),
(4, 'FT-101', 'Ruang Kuliah Teknik 1', 1, 'classroom', 50),
(4, 'FT-201', 'Lab Komputer 1', 2, 'laboratory', 40),
(4, 'FT-301', 'Ruang Seminar', 3, 'auditorium', 100);

-- ============================================
-- 7. VERIFICATION QUERIES
-- ============================================

SELECT 'Buildings' AS Tabel, COUNT(*) AS 'Total Data' FROM buildings
UNION ALL
SELECT 'Markers', COUNT(*) FROM markers
UNION ALL
SELECT 'Roads', COUNT(*) FROM roads
UNION ALL
SELECT 'Rooms', COUNT(*) FROM rooms
UNION ALL
SELECT 'Users', COUNT(*) FROM users;

-- ============================================
-- 8. SUCCESS MESSAGE
-- ============================================

SELECT '✅ Database navigasi_usu berhasil dibuat dan diisi dengan data lengkap!' AS Status;
SELECT 'Total Buildings: 10, Markers: 5, Roads: 21, Rooms: 7, Users: 2' AS Summary;
