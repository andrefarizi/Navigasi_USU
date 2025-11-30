-- Insert Sample Roads Data untuk Kampus USU
-- Koordinat berdasarkan area Universitas Sumatera Utara

-- Hapus data lama jika ada
DELETE FROM road_closures;
DELETE FROM roads;

-- Reset auto increment
ALTER TABLE roads AUTO_INCREMENT = 1;
ALTER TABLE road_closures AUTO_INCREMENT = 1;

-- Insert jalan-jalan utama di kampus USU
-- Koordinat USU: sekitar 3.565978, 98.658192
-- Struktur tabel: road_name, road_type ('main', 'secondary', 'pedestrian'), 
--                 start_lat, start_lng, end_lat, end_lng, is_one_way, direction, is_active

-- 1. Jl. Alumni (Jalan utama masuk kampus)
INSERT INTO roads (road_name, road_type, start_lat, start_lng, end_lat, end_lng, is_one_way, direction, is_active) VALUES
('Jl. Alumni (Gerbang Utama)', 'main', 3.5650, 98.6575, 3.5660, 98.6580, 0, 'normal', 1),
('Jl. Alumni (Tengah)', 'main', 3.5660, 98.6580, 3.5670, 98.6585, 0, 'normal', 1),
('Jl. Alumni (Dalam)', 'main', 3.5670, 98.6585, 3.5680, 98.6590, 0, 'normal', 1);

-- 2. Jl. Bioteknologi
INSERT INTO roads (road_name, road_type, start_lat, start_lng, end_lat, end_lng, is_one_way, direction, is_active) VALUES
('Jl. Bioteknologi 1', 'secondary', 3.5660, 98.6590, 3.5670, 98.6595, 0, 'normal', 1),
('Jl. Bioteknologi 2', 'secondary', 3.5670, 98.6595, 3.5680, 98.6600, 0, 'normal', 1);

-- 3. Jl. USU (Jalan dalam kampus)
INSERT INTO roads (road_name, road_type, start_lat, start_lng, end_lat, end_lng, is_one_way, direction, is_active) VALUES
('Jl. USU Utara', 'secondary', 3.5680, 98.6590, 3.5690, 98.6595, 0, 'normal', 1),
('Jl. USU Tengah', 'secondary', 3.5670, 98.6585, 3.5680, 98.6590, 0, 'normal', 1),
('Jl. USU Selatan', 'secondary', 3.5660, 98.6580, 3.5670, 98.6585, 0, 'normal', 1);

-- 4. Jl. Perpustakaan
INSERT INTO roads (road_name, road_type, start_lat, start_lng, end_lat, end_lng, is_one_way, direction, is_active) VALUES
('Jl. Perpustakaan', 'secondary', 3.5665, 98.6582, 3.5665, 98.6592, 0, 'normal', 1);

-- 5. Jl. Fakultas MIPA
INSERT INTO roads (road_name, road_type, start_lat, start_lng, end_lat, end_lng, is_one_way, direction, is_active) VALUES
('Jl. FMIPA', 'secondary', 3.5675, 98.6580, 3.5675, 98.6590, 0, 'normal', 1);

-- 6. Jl. Fakultas Teknik
INSERT INTO roads (road_name, road_type, start_lat, start_lng, end_lat, end_lng, is_one_way, direction, is_active) VALUES
('Jl. FT Utara', 'secondary', 3.5685, 98.6585, 3.5690, 98.6590, 0, 'normal', 1),
('Jl. FT Selatan', 'secondary', 3.5680, 98.6585, 3.5685, 98.6590, 0, 'normal', 1);

-- 7. Jl. Politeknik
INSERT INTO roads (road_name, road_type, start_lat, start_lng, end_lat, end_lng, is_one_way, direction, is_active) VALUES
('Jl. Politeknik', 'main', 3.5655, 98.6570, 3.5665, 98.6575, 0, 'normal', 1);

-- 8. Jl. Stadium Mini
INSERT INTO roads (road_name, road_type, start_lat, start_lng, end_lat, end_lng, is_one_way, direction, is_active) VALUES
('Jl. Stadium', 'secondary', 3.5670, 98.6600, 3.5680, 98.6605, 0, 'normal', 1);

-- 9. Jl. Rektorat
INSERT INTO roads (road_name, road_type, start_lat, start_lng, end_lat, end_lng, is_one_way, direction, is_active) VALUES
('Jl. Rektorat', 'secondary', 3.5668, 98.6588, 3.5672, 98.6592, 0, 'normal', 1);

-- 10. Jl. Auditorium
INSERT INTO roads (road_name, road_type, start_lat, start_lng, end_lat, end_lng, is_one_way, direction, is_active) VALUES
('Jl. Auditorium', 'secondary', 3.5662, 98.6586, 3.5665, 98.6590, 0, 'normal', 1);

-- 11. Jl. Mesjid Al-Makmur
INSERT INTO roads (road_name, road_type, start_lat, start_lng, end_lat, end_lng, is_one_way, direction, is_active) VALUES
('Jl. Mesjid', 'secondary', 3.5658, 98.6583, 3.5662, 98.6586, 0, 'normal', 1);

-- Tambahkan beberapa jalan penghubung
INSERT INTO roads (road_name, road_type, start_lat, start_lng, end_lat, end_lng, is_one_way, direction, is_active) VALUES
('Jl. Penghubung 1', 'secondary', 3.5665, 98.6580, 3.5670, 98.6582, 0, 'normal', 1),
('Jl. Penghubung 2', 'secondary', 3.5670, 98.6582, 3.5675, 98.6585, 0, 'normal', 1),
('Jl. Penghubung 3', 'secondary', 3.5675, 98.6587, 3.5680, 98.6589, 0, 'normal', 1),
('Jl. Lingkar Kampus', 'secondary', 3.5655, 98.6575, 3.5660, 98.6580, 0, 'normal', 1);

-- Contoh penutupan jalan (opsional - uncomment jika ingin test)
-- INSERT INTO road_closures (road_id, closure_type, reason, start_date, end_date, created_by) VALUES
-- (5, 'TEMPORARY', 'Perbaikan jalan', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1);

-- Verifikasi data
SELECT 'Total jalan yang ditambahkan:' AS info, COUNT(*) AS total FROM roads;
SELECT * FROM roads ORDER BY road_id;
