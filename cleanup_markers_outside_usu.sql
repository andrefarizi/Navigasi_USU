-- Script untuk hapus marker yang berada di luar area USU
-- Area USU: Latitude 3.555 - 3.575, Longitude 98.650 - 98.675

USE navigasi_usu;

-- Tampilkan marker yang akan dihapus (di luar area USU)
SELECT marker_id, marker_name, latitude, longitude, 
       CASE 
           WHEN latitude < 3.555 OR latitude > 3.575 THEN 'Latitude out of bounds'
           WHEN longitude < 98.650 OR longitude > 98.675 THEN 'Longitude out of bounds'
           ELSE 'OK'
       END as status
FROM markers
WHERE latitude < 3.555 OR latitude > 3.575 
   OR longitude < 98.650 OR longitude > 98.675;

-- Hapus marker yang berada di luar area USU
DELETE FROM markers 
WHERE latitude < 3.555 OR latitude > 3.575 
   OR longitude < 98.650 OR longitude > 98.675;

-- Tampilkan jumlah marker yang tersisa
SELECT COUNT(*) as total_markers_in_usu FROM markers;

-- Tampilkan semua marker yang masih ada
SELECT marker_id, marker_name, marker_type, latitude, longitude, description 
FROM markers 
ORDER BY marker_id;
