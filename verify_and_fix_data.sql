-- Script untuk verifikasi dan perbaikan data PETA_USU
-- Gunakan script ini untuk memastikan semua data ada dan benar

-- 1. Cek apakah ada data di tabel roads
SELECT 'Checking roads table...' AS status;
SELECT COUNT(*) AS total_roads FROM roads;
SELECT * FROM roads LIMIT 5;

-- 2. Cek apakah ada data di tabel road_closures
SELECT 'Checking road_closures table...' AS status;
SELECT COUNT(*) AS total_closures FROM road_closures;
SELECT * FROM road_closures LIMIT 5;

-- 3. Cek apakah ada data di tabel markers
SELECT 'Checking markers table...' AS status;
SELECT COUNT(*) AS total_markers FROM markers;
SELECT * FROM markers LIMIT 5;

-- 4. Cek apakah ada data di tabel buildings
SELECT 'Checking buildings table...' AS status;
SELECT COUNT(*) AS total_buildings FROM buildings;
SELECT * FROM buildings LIMIT 5;

-- 5. Jika data kosong, insert sample data untuk roads
-- Sample roads di area USU
INSERT IGNORE INTO roads (road_name, road_type, start_lat, start_lng, end_lat, end_lng, is_one_way, distance, description)
VALUES 
('Jl. Alumni', 'MAIN_ROAD', 3.565978, 98.658192, 3.566500, 98.660000, 0, 0.15, 'Jalan utama menuju kampus'),
('Jl. Bioteknologi', 'MAIN_ROAD', 3.566500, 98.660000, 3.567200, 98.660500, 0, 0.12, 'Jalan ke gedung Bioteknologi'),
('Jl. USU', 'MAIN_ROAD', 3.567200, 98.660500, 3.568000, 98.661000, 0, 0.13, 'Jalan dalam kampus USU');

-- 6. Verifikasi data sudah masuk
SELECT 'Verification after insert...' AS status;
SELECT COUNT(*) AS total_roads_after FROM roads;
SELECT * FROM roads;
