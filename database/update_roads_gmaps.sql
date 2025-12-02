-- ============================================
-- UPDATE ROADS TABLE - Google Maps Integration
-- Menambahkan kolom untuk polyline dan nama jalan dari Google Maps API
-- ============================================

USE navigasi_usu;

-- Add new columns to roads table
ALTER TABLE roads 
ADD COLUMN polyline_points TEXT COMMENT 'Encoded polyline from Google Maps API',
ADD COLUMN google_road_name VARCHAR(200) COMMENT 'Road name from Google Maps API (e.g., Jl. Alumni)',
ADD COLUMN road_segments JSON COMMENT 'Array of road segments with detailed polylines',
ADD COLUMN last_gmaps_update TIMESTAMP NULL COMMENT 'Last time Google Maps data was fetched',
ADD INDEX idx_google_road_name (google_road_name);

-- Update comment on table
ALTER TABLE roads 
COMMENT = 'Jalan-jalan di USU dengan integrasi Google Maps API untuk routing akurat';

-- Sample update script untuk existing roads (contoh)
-- UPDATE roads 
-- SET google_road_name = 'Jl. Alumni', 
--     last_gmaps_update = NOW() 
-- WHERE road_name LIKE '%Alumni%';

SELECT 'Tabel roads berhasil diupdate dengan kolom Google Maps!' AS Status;
SELECT 'Kolom baru: polyline_points, google_road_name, road_segments, last_gmaps_update' AS Info;

-- Verify new columns
DESCRIBE roads;
