-- Script Lengkap: Insert Data Buildings, Markers, dan Roads untuk PETA USU
-- Jalankan script ini di phpMyAdmin atau MySQL Workbench

USE navigasi_usu;

-- ===================================================
-- 1. HAPUS DATA LAMA (jika ada)
-- ===================================================
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM road_closures;
DELETE FROM roads;
DELETE FROM markers;
DELETE FROM buildings;
SET FOREIGN_KEY_CHECKS = 1;

-- Reset auto increment
ALTER TABLE buildings AUTO_INCREMENT = 1;
ALTER TABLE markers AUTO_INCREMENT = 1;
ALTER TABLE roads AUTO_INCREMENT = 1;
ALTER TABLE road_closures AUTO_INCREMENT = 1;

-- ===================================================
-- 2. INSERT BUILDINGS (Gedung-gedung di USU)
-- ===================================================
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

-- ===================================================
-- 3. INSERT MARKERS (Lokasi penting di USU)
-- ===================================================
INSERT INTO markers (marker_name, marker_type, description, latitude, longitude, is_active) VALUES
('Parkir Gedung Rektorat', 'parking', 'Area parkir gedung rektorat', 3.5667, 98.6590, 1),
('Kantin Fakultas MIPA', 'kantin', 'Kantin untuk mahasiswa MIPA', 3.5676, 98.6583, 1),
('Taman Kampus', 'taman', 'Taman untuk refreshing mahasiswa', 3.5672, 98.6586, 1),
('Halte Bus Kampus', 'transportasi', 'Halte bus untuk transportasi mahasiswa', 3.5652, 98.6578, 1),
('ATM Center', 'fasilitas', 'ATM Center untuk mahasiswa', 3.5662, 98.6584, 1);

-- ===================================================
-- 4. INSERT ROADS (Jalan-jalan di USU)
-- ===================================================
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

-- ===================================================
-- 5. VERIFIKASI DATA
-- ===================================================
SELECT 'Buildings' AS Tabel, COUNT(*) AS 'Total Data' FROM buildings
UNION ALL
SELECT 'Markers', COUNT(*) FROM markers
UNION ALL
SELECT 'Roads', COUNT(*) FROM roads;

-- Tampilkan semua data
SELECT '===== BUILDINGS =====' AS Info;
SELECT building_id, building_code, building_name, building_type, latitude, longitude FROM buildings ORDER BY building_id;

SELECT '===== MARKERS =====' AS Info;
SELECT marker_id, marker_name, marker_type, latitude, longitude FROM markers ORDER BY marker_id;

SELECT '===== ROADS =====' AS Info;
SELECT road_id, road_name, road_type, is_one_way FROM roads ORDER BY road_id;

SELECT '✅ Script executed successfully! Data inserted into navigasi_usu database.' AS Result;
