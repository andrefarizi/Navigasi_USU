-- ============================================
-- FIX MARKER COORDINATES
-- Memperbaiki koordinat marker yang salah
-- Universitas Sumatera Utara berada di sekitar:
-- Latitude: 3.55 - 3.58
-- Longitude: 98.65 - 98.67
-- ============================================

USE navigasi_usu;

-- Tampilkan marker yang ada sebelum update
SELECT marker_id, marker_name, latitude, longitude, 
       CASE 
           WHEN latitude < 3.55 OR latitude > 3.58 OR longitude < 98.65 OR longitude > 98.67 
           THEN 'INVALID - Di luar area USU!'
           ELSE 'VALID'
       END as status
FROM markers
WHERE is_active = TRUE;

-- Update marker 'aldrik' dengan koordinat di JALAN yang benar
-- Koordinat ini adalah titik di Jalan Dr. Mansyur (akses utama USU) yang DIKENAL Google Maps
UPDATE markers 
SET latitude = 3.5693782,   -- Jl. Dr. Mansyur dekat Gerbang Utama USU
    longitude = 98.6559048,
    description = 'Marker test aldrik - Jl. Dr. Mansyur (Updated)',
    updated_at = CURRENT_TIMESTAMP
WHERE marker_name = 'aldrik';

-- Update marker 'halah' dengan koordinat di JALAN yang benar
-- Koordinat ini adalah titik di Jalan Alumni/Prof. Hamka (jalan dalam kampus) yang DIKENAL Google Maps
UPDATE markers 
SET latitude = 3.5643782,    -- Jl. Prof. Hamka / Jl. Alumni USU (~500m dari aldrik)
    longitude = 98.6547048,
    description = 'Marker test halah - Jl. Alumni USU (Updated)',  
    updated_at = CURRENT_TIMESTAMP
WHERE marker_name = 'halah' OR marker_name = 'hahah';

-- Tampilkan hasil update
SELECT marker_id, marker_name, latitude, longitude, 
       ROUND(latitude, 6) as lat_rounded,
       ROUND(longitude, 6) as lng_rounded,
       description,
       updated_at,
       CASE 
           WHEN latitude >= 3.55 AND latitude <= 3.58 AND longitude >= 98.65 AND longitude <= 98.67
           THEN '✓ VALID - Dalam area USU'
           ELSE '✗ INVALID - Di luar area USU'
       END as validation_status
FROM markers
WHERE marker_name IN ('aldrik', 'halah', 'hahah')
ORDER BY marker_id;

-- Koordinat referensi untuk area USU:
-- ============================================
-- Gedung Rektorat USU: 3.5693° N, 98.6564° E
-- Fakultas Teknik: 3.5711° N, 98.6547° E  
-- Fakultas Ekonomi: 3.5679° N, 98.6572° E
-- Fakultas MIPA: 3.5660° N, 98.6535° E
-- Masjid Al-Ma'arif: 3.5672° N, 98.6553° E
-- ============================================

-- Jika ingin menghapus marker test yang salah dan insert ulang:
/*
DELETE FROM markers WHERE marker_name IN ('aldrik', 'halah', 'hahah');

INSERT INTO markers (marker_name, marker_type, description, latitude, longitude, icon_path, icon_name, is_active, created_by) VALUES
('aldrik', 'test', 'Marker test di area Rektorat USU', 3.5693, 98.6564, NULL, NULL, TRUE, 1),
('halah', 'test', 'Marker test di area selatan kampus USU', 3.5685, 98.6550, NULL, NULL, TRUE, 1);
*/
